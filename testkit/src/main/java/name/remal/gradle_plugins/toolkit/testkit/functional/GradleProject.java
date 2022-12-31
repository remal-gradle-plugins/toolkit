package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.NONE;
import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionGreaterThanOrEqualTo;
import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionLessThan;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.startsWithString;
import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.StringUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.Contract;

@Getter
public class GradleProject extends BaseGradleProject<GradleProject> {

    private static final Duration DEFAULT_TASK_TIMEOUT = Duration.ofMinutes(1);

    private static final GradleVersion MIN_GRADLE_VERSION_WITH_CONFIGURATION_CACHE = GradleVersion.version("6.6");


    protected final Map<String, GradleChildProject> children = synchronizedMap(new LinkedHashMap<>());

    protected final SettingsFile settingsFile;

    public GradleProject(File projectDir) {
        super(projectDir.getAbsoluteFile());
        this.settingsFile = new SettingsFile(this.projectDir);
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject forSettingsFile(Consumer<SettingsFile> settingsFileConsumer) {
        settingsFileConsumer.accept(this.settingsFile);
        return this;
    }

    public final synchronized GradleChildProject newChildProject(String name) {
        return children.computeIfAbsent(name, __ -> {
            val childProjectDir = new File(projectDir, name);
            val child = new GradleChildProject(childProjectDir);
            settingsFile.append("include(':" + escapeGroovy(child.getName()) + "')");

            child.buildFile.setTaskTimeout(taskTimeout);

            return child;
        });
    }

    @Contract("_,_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject newChildProject(
        String name,
        Consumer<GradleChildProject> childProjectConsumer
    ) {
        val child = newChildProject(name);
        childProjectConsumer.accept(child);
        return this;
    }


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
        SuppressedMessage.builder()
            .message("The DefaultSourceDirectorySet constructor has been deprecated")
            .stackTracePackagePrefix("org.jetbrains.kotlin.gradle.plugin.")
            .build(),
        SuppressedMessage.builder()
            .message("Classpath configuration has been deprecated for dependency declaration")
            .stackTracePackagePrefix("org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinCompilation.")
            .build(),
        SuppressedMessage.builder()
            .message("Internal API constructor TaskReportContainer(Class<T>, Task) has been deprecated")
            .stackTracePackagePrefix("com.github.spotbugs.")
            .build(),
        SuppressedMessage.builder()
            .message("BuildListener#buildStarted(Gradle) has been deprecated")
            .stackTracePackagePrefix("org.jetbrains.gradle.ext.")
            .build(),
        SuppressedMessage.builder()
            .message("org.gradle.util.ConfigureUtil type has been deprecated")
            .stackTracePackagePrefix("io.github.gradlenexus.publishplugin.")
            .build()
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


    private static final List<String> DEFAULT_CONFIGURATION_CACHE_WARNINGS = ImmutableList.of(
        "problem was found storing the configuration cache",
        "problems were found storing the configuration cache"
    );

    @Getter(NONE)
    private final List<String> configurationCacheWarnings =
        new ArrayList<>(DEFAULT_CONFIGURATION_CACHE_WARNINGS);

    private static final List<SuppressedMessage> DEFAULT_SUPPRESSED_CONFIGURATION_CACHE_WARNINGS = ImmutableList.of(
        SuppressedMessage.builder()
            .startsWith(true)
            .message("0 problems were found storing the configuration cache")
            .build()
    );

    @Getter(NONE)
    private final List<SuppressedMessage> suppressedConfigurationCacheWarnings =
        new ArrayList<>(DEFAULT_SUPPRESSED_CONFIGURATION_CACHE_WARNINGS);


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

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addConfigurationCacheWarning(String message) {
        assertIsNotBuilt();
        configurationCacheWarnings.add(message);
        return this;
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addSuppressedConfigurationCacheWarning(
        SuppressedMessage suppressedMessage
    ) {
        assertIsNotBuilt();
        suppressedConfigurationCacheWarnings.add(suppressedMessage);
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


    private boolean withConfigurationCache =
        isCurrentGradleVersionGreaterThanOrEqualTo(MIN_GRADLE_VERSION_WITH_CONFIGURATION_CACHE);

    @Contract("-> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject withoutConfigurationCache() {
        assertIsNotBuilt();
        this.withConfigurationCache = false;
        return this;
    }


    @Nullable
    private Duration taskTimeout;

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject setTaskTimeout(@Nullable Duration timeout) {
        this.taskTimeout = timeout;
        buildFile.setTaskTimeout(taskTimeout);
        children.values().forEach(child -> {
            child.buildFile.setTaskTimeout(taskTimeout);
        });
        return this;
    }

    {
        setTaskTimeout(DEFAULT_TASK_TIMEOUT);
    }


    public final BuildResult assertBuildSuccessfully() {
        return build(true);
    }

    public final BuildResult assertBuildFails() {
        return build(false);
    }

    @Nullable
    private BuildResult buildResult;
    @Nullable
    private Throwable buildException;

    @SneakyThrows
    @SuppressWarnings({"java:S106", "UnstableApiUsage"})
    private synchronized BuildResult build(boolean isExpectingSuccess) {
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

                if (isExpectingSuccess) {
                    currentBuildResult = runner.build();
                } else {
                    currentBuildResult = runner.buildAndFail();
                }

                val output = currentBuildResult.getOutput();
                List<String> outputLines = Splitter.onPattern("[\\n\\r]+").splitToStream(output)
                    .map(StringUtils::trimRight)
                    .filter(not(String::isEmpty))
                    .collect(toList());
                assertNoDeprecationMessages(outputLines);
                assertNoMutableProjectStateWarnings(outputLines);
                assertNoOptimizationsDisabledWarnings(outputLines);
                assertNoConfigurationCacheWarnings(outputLines);

            } catch (Throwable e) {
                buildException = e;
                throw e;
            }
            buildResult = currentBuildResult;
        }

        return buildResult;
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
            //.withDebug(isDebugEnabled())
            .withArguments(
                "--stacktrace",
                "--warning-mode=all",
                "-Dorg.gradle.parallel=true",
                "-Dorg.gradle.workers.max=4",
                "-Dorg.gradle.vfs.watch=false",
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

        if (withConfigurationCache) {
            if (isConfigurationCacheSupportedWithAppliedPlugins()) {
                runner.withArguments(Stream.concat(
                    runner.getArguments().stream(),
                    Stream.of(
                        "--configuration-cache",
                        "--configuration-cache-problems=warn"
                    )
                ).collect(toList()));
            }
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

    private boolean isConfigurationCacheSupportedWithAppliedPlugins() {
        if (isCurrentGradleVersionLessThan("7.2")) {
            if (isPluginAppliedForAnyProject("groovy")
                || isPluginAppliedForAnyProject("scala")
            ) {
                return false;
            }
        }

        if (isCurrentGradleVersionLessThan("6.8")) {
            if (isPluginAppliedForAnyProject("checkstyle")
                || isPluginAppliedForAnyProject("pmd")
                || isPluginAppliedForAnyProject("jacoco")
                || isPluginAppliedForAnyProject("codenarc")
            ) {
                return false;
            }
        }

        return true;
    }

    private boolean isPluginAppliedForAnyProject(String pluginId) {
        if (getSettingsFile().isPluginApplied(pluginId)
            || getBuildFile().isPluginApplied(pluginId)
        ) {
            return true;
        }

        val isAppliedToChild = getChildren().values().stream()
            .anyMatch(child -> child.getBuildFile().isPluginApplied(pluginId));
        return isAppliedToChild;
    }

    private void assertNoDeprecationMessages(List<String> outputLines) {
        val errors = parseErrors(
            outputLines,
            deprecationMessages,
            suppressedDeprecationMessages,
            null
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
            suppressedMutableProjectStateWarnings,
            null
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
            suppressedOptimizationsDisabledWarnings,
            null
        );
        if (!errors.isEmpty()) {
            val sb = new StringBuilder();
            sb.append("Optimizations disabled warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    private void assertNoConfigurationCacheWarnings(List<String> outputLines) {
        val errors = parseErrors(
            outputLines,
            configurationCacheWarnings,
            suppressedConfigurationCacheWarnings,
            startsWithString("See the complete report at ")
        );
        if (!errors.isEmpty()) {
            val sb = new StringBuilder();
            sb.append("Configuration cache warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    @SuppressWarnings({"java:S3776", "java:S127"})
    private Collection<String> parseErrors(
        List<String> outputLines,
        List<String> messages,
        List<SuppressedMessage> suppressedMessages,
        @Nullable Predicate<String> isWarningEndLine
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
                boolean matches;
                if (suppressedMessage.isStartsWith()) {
                    matches = line.startsWith(suppressedMessage.getMessage());
                } else {
                    matches = line.contains(suppressedMessage.getMessage());
                }
                if (matches) {
                    val stackTracePackagePrefix = suppressedMessage.getStackTracePackagePrefix();
                    if (isEmpty(stackTracePackagePrefix)) {
                        continue forEachLine;
                    }

                    for (int i = lineIndex + 1; i < outputLines.size(); ++i) {
                        val stackTraceLine = outputLines.get(i);
                        if (!STACK_TRACE_LINE.matcher(stackTraceLine).find()) {
                            break;
                        }
                        if (stackTraceLine.contains("at " + stackTracePackagePrefix)) {
                            continue forEachLine;
                        }
                    }
                }
            }

            if (isWarningEndLine == null) {
                errors.add(line);
                continue;
            }

            val message = new StringBuilder();
            message.append(line);
            for (++lineIndex; lineIndex < outputLines.size(); ++lineIndex) {
                val nextLine = outputLines.get(lineIndex);
                message.append('\n').append(nextLine);
                if (isWarningEndLine.test(nextLine)) {
                    break;
                }
            }
            errors.add(message.toString());
        }

        return errors;
    }

}