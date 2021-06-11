package name.remal.gradleplugins.testkit.functional;

import static java.lang.String.format;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static name.remal.gradleplugins.toolkit.StringEscapeUtils.escapeGroovy;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import org.gradle.initialization.StartParameterBuildOptions.WatchFileSystemOption;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.Contract;

@Getter
public class GradleProject extends BaseGradleProject<GradleProject> {

    protected final Map<String, GradleChildProject> children = synchronizedMap(new LinkedHashMap<>());

    protected final SettingsFile settingsFile;

    public GradleProject(File projectDir) {
        super(projectDir);
        this.settingsFile = new SettingsFile(this.projectDir);
    }

    @Contract("_ -> this")
    public final GradleProject forSettingsFile(Consumer<SettingsFile> settingsFileConsumer) {
        settingsFileConsumer.accept(this.settingsFile);
        return this;
    }

    public final GradleChildProject newChildProject(String name) {
        return children.computeIfAbsent(name, __ -> {
            val childProjectDir = new File(projectDir, name);
            val child = new GradleChildProject(childProjectDir);
            settingsFile.append("include(':" + escapeGroovy(child.getName()) + "')");
            return child;
        });
    }

    @Contract("_,_ -> this")
    public final GradleProject newChildProject(String name, Consumer<GradleChildProject> childProjectConsumer) {
        val child = newChildProject(name);
        childProjectConsumer.accept(child);
        return this;
    }


    private static final boolean IS_IN_DEBUG = getRuntimeMXBean().getInputArguments().toString().contains("jdwp");
    private static final Pattern TRIM_RIGHT = Pattern.compile("\\s+$");
    private static final Pattern STACK_TRACE_LINE = Pattern.compile("^\\s+at ");

    private static final List<String> DEPRECATION_MESSAGES = unmodifiableList(asList(
        "has been deprecated and is scheduled to be removed in Gradle",
        "Deprecated Gradle features were used in this build",
        "is scheduled to be removed in Gradle",
        "will fail with an error in Gradle"
    ));

    private static final List<SuppressedDeprecation> SUPPRESSED_DEPRECATIONS = unmodifiableList(asList(
        new SuppressedDeprecation(
            "The DefaultSourceDirectorySet constructor has been deprecated",
            "org.jetbrains.kotlin.gradle.plugin."
        ),
        new SuppressedDeprecation(
            "Classpath configuration has been deprecated for dependency declaration",
            "org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinCompilation."
        ),
        new SuppressedDeprecation(
            "Internal API constructor TaskReportContainer(Class<T>, Task) has been deprecated",
            "com.github.spotbugs."
        )
    ));

    private static final List<String> MUTABLE_PROJECT_STATE_WARNING_MESSAGES = unmodifiableList(asList(
        "was resolved without accessing the project in a safe manner",
        "configuration is resolved from a thread not managed by Gradle"
    ));

    private BuildResult buildResult;
    private Throwable buildException;

    @SneakyThrows
    public synchronized BuildResult build() {
        if (buildException != null) {
            throw buildException;
        }

        if (buildResult == null) {
            final BuildResult currentBuildResult;
            try {
                buildFile.writeToDisk();
                settingsFile.writeToDisk();
                children.values().forEach(child -> child.getBuildFile().writeToDisk());

                val runner = createGradleRunner();
                currentBuildResult = runner.build();

                val output = buildResult.getOutput();
                List<String> outputLines = Stream.of(output.split("\n"))
                    .map(it -> TRIM_RIGHT.matcher(it).replaceFirst(""))
                    .filter(it -> !it.isEmpty())
                    .collect(toList());
                assertNoDeprecationMessages(outputLines);
                assertNoMutableProjectStateWarnings(outputLines);

            } catch (Throwable e) {
                buildException = e;
                throw e;
            }
            buildResult = currentBuildResult;
        }

        return buildResult;
    }

    public void assertBuildSuccessfully() {
        build();
    }

    private boolean withPluginClasspath = true;

    @Contract("-> this")
    public GradleProject withoutPluginClasspath() {
        this.withPluginClasspath = false;
        return this;
    }

    @SneakyThrows
    private GradleRunner createGradleRunner() {
        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .forwardOutput()
            .withDebug(IS_IN_DEBUG)
            .withArguments(
                "--stacktrace",
                "--warning-mode=all",
                "-Dorg.gradle.parallel=true",
                "-Dorg.gradle.workers.max=4",
                format("-D%s=false", WatchFileSystemOption.GRADLE_PROPERTY),
                "-Dhttp.keepAlive=false",
                "-Dsun.net.http.retryPost=false",
                "-Dsun.io.useCanonCaches=false",
                "-Dsun.net.client.defaultConnectTimeout=15000",
                "-Dsun.net.client.defaultReadTimeout=600000",
                "-Djava.awt.headless=true",
                "-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false"
            );

        if (withPluginClasspath) {
            runner.withPluginClasspath();
        }

        String gradleDistribMirror = System.getenv("GRADLE_DISTRIBUTIONS_MIRROR");
        if (gradleDistribMirror == null || gradleDistribMirror.isEmpty()) {
            runner.withGradleVersion(GradleVersion.current().getVersion());
        } else {
            while (gradleDistribMirror.endsWith("/")) {
                gradleDistribMirror = gradleDistribMirror.substring(0, gradleDistribMirror.length() - 1);
            }
            val distributionUri = new URI(gradleDistribMirror + format(
                "/gradle-%s-bin.zip",
                GradleVersion.current().getVersion()
            ));
            runner.withGradleDistribution(distributionUri);
        }

        return runner;
    }

    @SuppressWarnings("java:S3776")
    private void assertNoDeprecationMessages(List<String> outputLines) {
        Collection<String> deprecations = new LinkedHashSet<>();
        forEachLine:
        for (int lineIndex = 0; lineIndex < outputLines.size(); ++lineIndex) {
            val line = outputLines.get(lineIndex);
            val hasWarning = DEPRECATION_MESSAGES.stream().anyMatch(line::contains);
            if (!hasWarning) {
                continue;
            }

            for (val suppressedDeprecation : SUPPRESSED_DEPRECATIONS) {
                if (line.contains(suppressedDeprecation.getMessage())) {
                    for (int i = lineIndex + 1; i < outputLines.size(); ++i) {
                        val stackTraceLine = outputLines.get(i);
                        if (!STACK_TRACE_LINE.matcher(stackTraceLine).find()) {
                            break;
                        }
                        val stackTracePackagePrefix = suppressedDeprecation.getStackTracePackagePrefix();
                        if (stackTracePackagePrefix != null
                            && !stackTracePackagePrefix.isEmpty()
                            && stackTraceLine.contains(stackTracePackagePrefix)
                        ) {
                            continue forEachLine;
                        }
                    }
                }
            }

            deprecations.add(line);
        }
        if (!deprecations.isEmpty()) {
            val sb = new StringBuilder();
            sb.append("Deprecation warnings were found:");
            deprecations.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    private void assertNoMutableProjectStateWarnings(List<String> outputLines) {
        Collection<String> mutableProjectStateWarnings = new LinkedHashSet<>();
        for (final String line : outputLines) {
            val hasWarning = MUTABLE_PROJECT_STATE_WARNING_MESSAGES.stream().anyMatch(line::contains);
            if (hasWarning) {
                mutableProjectStateWarnings.add(line);
            }
        }
        if (!mutableProjectStateWarnings.isEmpty()) {
            val sb = new StringBuilder();
            sb.append("Mutable Project State warnings were found:");
            mutableProjectStateWarnings.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    @Value
    @RequiredArgsConstructor
    private static class SuppressedDeprecation {
        String message;
        @Nullable
        String stackTracePackagePrefix;
    }

}
