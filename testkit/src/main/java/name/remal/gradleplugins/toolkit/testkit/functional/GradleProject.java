package name.remal.gradleplugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.NONE;
import static name.remal.gradleplugins.toolkit.StringUtils.escapeGroovy;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
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
import lombok.SneakyThrows;
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
    @CanIgnoreReturnValue
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
    @CanIgnoreReturnValue
    public final GradleProject newChildProject(String name, Consumer<GradleChildProject> childProjectConsumer) {
        val child = newChildProject(name);
        childProjectConsumer.accept(child);
        return this;
    }


    private static final boolean IS_IN_DEBUG = getRuntimeMXBean().getInputArguments().toString().contains("jdwp");
    private static final Pattern TRIM_RIGHT = Pattern.compile("\\s+$");
    private static final Pattern STACK_TRACE_LINE = Pattern.compile("^\\s+at ");

    private static final List<String> DEFAULT_DEPRECATION_MESSAGES = ImmutableList.of(
        "has been deprecated and is scheduled to be removed in Gradle",
        "Deprecated Gradle features were used in this build",
        "is scheduled to be removed in Gradle",
        "will fail with an error in Gradle"
    );

    @Getter(NONE)
    private final List<String> deprecationMessages = new ArrayList<>(DEFAULT_DEPRECATION_MESSAGES);

    private static final List<SuppressedMessage> DEFAULT_SUPPRESSED_DEPRECATIONS_MESSAGES = ImmutableList.of(
        new SuppressedMessage(
            "The DefaultSourceDirectorySet constructor has been deprecated",
            "org.jetbrains.kotlin.gradle.plugin."
        ),
        new SuppressedMessage(
            "Classpath configuration has been deprecated for dependency declaration",
            "org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinCompilation."
        ),
        new SuppressedMessage(
            "Internal API constructor TaskReportContainer(Class<T>, Task) has been deprecated",
            "com.github.spotbugs."
        ),
        new SuppressedMessage(
            "BuildListener#buildStarted(Gradle) has been deprecated",
            "org.jetbrains.gradle.ext."
        )
    );

    @Getter(NONE)
    private final List<SuppressedMessage> suppressedDeprecationMessages =
        new ArrayList<>(DEFAULT_SUPPRESSED_DEPRECATIONS_MESSAGES);


    private static final List<String> DEFAULT_MUTABLE_PROJECT_STATE_WARNINGS = ImmutableList.of(
        "was resolved without accessing the project in a safe manner",
        "configuration is resolved from a thread not managed by Gradle"
    );

    @Getter(NONE)
    private final List<String> mutableProjectStateWarnings =
        new ArrayList<>(DEFAULT_MUTABLE_PROJECT_STATE_WARNINGS);

    private static final List<SuppressedMessage> DEFAULT_SUPPRESSED_MUTABLE_PROJECT_STATE_WARNINGS =
        emptyList();

    @Getter(NONE)
    private final List<SuppressedMessage> suppressedMutableProjectStateWarnings =
        new ArrayList<>(DEFAULT_SUPPRESSED_MUTABLE_PROJECT_STATE_WARNINGS);


    private static final List<String> DEFAULT_OPTIMIZATIONS_DISABLED_WARNINGS = ImmutableList.of(
        "Execution optimizations have been disabled for task",
        "This can lead to incorrect results being produced, depending on what order the tasks are executed"
    );

    @Getter(NONE)
    private final List<String> optimizationsDisabledWarnings =
        new ArrayList<>(DEFAULT_OPTIMIZATIONS_DISABLED_WARNINGS);

    private static final List<SuppressedMessage> DEFAULT_SUPPRESSED_OPTIMIZATIONS_DISABLED_WARNINGS =
        emptyList();

    @Getter(NONE)
    private final List<SuppressedMessage> suppressedOptimizationsDisabledWarnings =
        new ArrayList<>(DEFAULT_SUPPRESSED_OPTIMIZATIONS_DISABLED_WARNINGS);


    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addDeprecationMessage(String message) {
        assertIsNotBuilt();
        deprecationMessages.add(message);
        return this;
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addSuppressedDeprecationMessage(SuppressedMessage suppressedMessage) {
        assertIsNotBuilt();
        suppressedDeprecationMessages.add(suppressedMessage);
        return this;
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addMutableProjectStateWarning(String message) {
        assertIsNotBuilt();
        mutableProjectStateWarnings.add(message);
        return this;
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addSuppressedMutableProjectStateWarning(
        SuppressedMessage suppressedMessage
    ) {
        assertIsNotBuilt();
        suppressedMutableProjectStateWarnings.add(suppressedMessage);
        return this;
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addOptimizationsDisabledWarning(String message) {
        assertIsNotBuilt();
        optimizationsDisabledWarnings.add(message);
        return this;
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addSuppressedOptimizationsDisabledWarning(
        SuppressedMessage suppressedMessage
    ) {
        assertIsNotBuilt();
        suppressedOptimizationsDisabledWarnings.add(suppressedMessage);
        return this;
    }


    private boolean withPluginClasspath = true;

    @Contract("-> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject withoutPluginClasspath() {
        assertIsNotBuilt();
        this.withPluginClasspath = false;
        return this;
    }


    @Nullable
    private BuildResult buildResult;
    @Nullable
    private Throwable buildException;

    @SneakyThrows
    public final synchronized BuildResult build() {
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

                val output = currentBuildResult.getOutput();
                List<String> outputLines = Stream.of(output.split("\n"))
                    .map(it -> TRIM_RIGHT.matcher(it).replaceFirst(""))
                    .filter(it -> !it.isEmpty())
                    .collect(toList());
                assertNoDeprecationMessages(outputLines);
                assertNoMutableProjectStateWarnings(outputLines);
                assertNoOptimizationsDisabledWarnings(outputLines);

            } catch (Throwable e) {
                buildException = e;
                throw e;
            }
            buildResult = currentBuildResult;
        }

        return buildResult;
    }

    public final synchronized void assertBuildSuccessfully() {
        build();
    }

    private void assertIsNotBuilt() {
        if (buildException != null || buildResult != null) {
            throw new IllegalStateException("The project has already been built");
        }
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

    private void assertNoDeprecationMessages(List<String> outputLines) {
        val errors = parseErrors(
            outputLines,
            deprecationMessages,
            suppressedDeprecationMessages
        );
        if (!errors.isEmpty()) {
            val sb = new StringBuilder();
            sb.append("Deprecation warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    private void assertNoMutableProjectStateWarnings(List<String> outputLines) {
        val errors = parseErrors(
            outputLines,
            mutableProjectStateWarnings,
            suppressedMutableProjectStateWarnings
        );
        if (!errors.isEmpty()) {
            val sb = new StringBuilder();
            sb.append("Mutable Project State warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    private void assertNoOptimizationsDisabledWarnings(List<String> outputLines) {
        val errors = parseErrors(
            outputLines,
            optimizationsDisabledWarnings,
            suppressedOptimizationsDisabledWarnings
        );
        if (!errors.isEmpty()) {
            val sb = new StringBuilder();
            sb.append("Optimizations disabled warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    @SuppressWarnings("java:S3776")
    private Collection<String> parseErrors(
        List<String> outputLines,
        List<String> messages,
        List<SuppressedMessage> suppressedMessages
    ) {
        Collection<String> errors = new LinkedHashSet<>();
        forEachLine:
        for (int lineIndex = 0; lineIndex < outputLines.size(); ++lineIndex) {
            val line = outputLines.get(lineIndex);
            val hasError = messages.stream().anyMatch(line::contains);
            if (!hasError) {
                continue;
            }

            for (val suppressedMessage : suppressedMessages) {
                if (line.contains(suppressedMessage.getMessage())) {
                    for (int i = lineIndex + 1; i < outputLines.size(); ++i) {
                        val stackTraceLine = outputLines.get(i);
                        if (!STACK_TRACE_LINE.matcher(stackTraceLine).find()) {
                            break;
                        }
                        val stackTracePackagePrefix = suppressedMessage.getStackTracePackagePrefix();
                        if (stackTracePackagePrefix != null
                            && !stackTracePackagePrefix.isEmpty()
                            && stackTraceLine.contains("at " + stackTracePackagePrefix)
                        ) {
                            continue forEachLine;
                        }
                    }
                }
            }

            errors.add(line);
        }

        return errors;
    }

}
