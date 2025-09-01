package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.NONE;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.SUPPORTED;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.UNSUPPORTED;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityUtils.getGradleJavaCompatibility;
import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionGreaterThanOrEqualTo;
import static name.remal.gradle_plugins.toolkit.InTestFlags.IS_IN_FUNCTIONAL_TEST_ENV_VAR;
import static name.remal.gradle_plugins.toolkit.InTestFlags.IS_IN_TEST_ENV_VAR;
import static name.remal.gradle_plugins.toolkit.JacocoJvmArg.currentJvmArgsHaveJacocoJvmArg;
import static name.remal.gradle_plugins.toolkit.JacocoJvmArg.parseJacocoJvmArgFromCurrentJvmArgs;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.PathUtils.copyRecursively;
import static name.remal.gradle_plugins.toolkit.PathUtils.deleteRecursively;
import static name.remal.gradle_plugins.toolkit.StringUtils.trimRightWith;
import static name.remal.gradle_plugins.toolkit.testkit.functional.GradleRunnerUtils.withJvmArguments;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.toolkit.ConfigurationCacheUtils;
import name.remal.gradle_plugins.toolkit.StringUtils;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleBuildFileContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleSettingsFileContent;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.jspecify.annotations.Nullable;

@Getter
public abstract class AbstractGradleProject<
    Block extends JavaLikeContent<Block>,
    BuildFileType extends GradleBuildFileContent<Block>,
    SettingsFileType extends GradleSettingsFileContent<Block>,
    Child extends AbstractChildGradleProject<Block, ? extends GradleBuildFileContent<Block>>
    > extends AbstractBaseGradleProject<Block, BuildFileType> {

    @ForOverride
    protected abstract SettingsFileType createSettingsFileContent();

    @ForOverride
    protected abstract String getSettingsFileName();

    @ForOverride
    protected abstract Child createChildProject(File childProjectDir);

    @ForOverride
    protected abstract void injectJacocoDumperImpl();


    private static final GradleVersion MIN_GRADLE_VERSION_WITH_RUNNER_ENVIRONMENT = GradleVersion.version("5.2");
    private static final GradleVersion MIN_GRADLE_VERSION_WITH_CONFIGURATION_CACHE = GradleVersion.version("6.6");


    protected final Map<String, Child> children = new LinkedHashMap<>();

    protected final SettingsFileType settingsFile;

    protected AbstractGradleProject(File projectDir) {
        super(projectDir);
        this.settingsFile = createSettingsFileContent();
    }


    public void forSettingsFile(Action<SettingsFileType> action) {
        action.execute(settingsFile);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void writeToDisk() {
        super.writeToDisk();
        writeTextFile(getSettingsFileName(), settingsFile.toString());
        children.values().forEach(AbstractBaseGradleProject::writeToDisk);
    }

    public Child newChildProject(String name) {
        return children.computeIfAbsent(name, __ -> {
            var childProjectDir = new File(projectDir, name);
            var child = createChildProject(childProjectDir);
            settingsFile.line("include(\":%s\")", settingsFile.escapeString(child.getName()));
            return child;
        });
    }

    public Child newChildProject(String name, Action<Child> action) {
        var child = newChildProject(name);
        action.execute(child);
        return child;
    }


    @Getter(NONE)
    private final List<String> forbiddenMessages = new ArrayList<>();

    @Getter(NONE)
    private final List<SuppressedMessage> suppressedForbiddenMessages = new ArrayList<>();

    private static final List<String> DEFAULT_DEPRECATION_MESSAGES = List.of(
        "has been deprecated and is scheduled to be removed in Gradle",
        "Deprecated Gradle features were used in this build",
        "is scheduled to be removed in Gradle",
        "will fail with an error in Gradle"
    );

    @Getter(NONE)
    private final List<String> deprecationMessages = new ArrayList<>(DEFAULT_DEPRECATION_MESSAGES);

    private static final List<SuppressedMessage> DEFAULT_SUPPRESSED_DEPRECATIONS_MESSAGES = List.of(
        SuppressedMessage.builder()
            .startsWith(true)
            .message("Java toolchain auto-provisioning enabled"
                + ", but no java toolchain repositories declared by the build"
            )
            .stackTracePrefix("org.gradle.jvm.toolchain.")
            .build(),
        SuppressedMessage.builder()
            .startsWith(true)
            .message("Using a toolchain installed via auto-provisioning"
                + ", but having no toolchain repositories configured"
            )
            .stackTracePrefix("org.gradle.jvm.toolchain.")
            .build(),
        SuppressedMessage.builder()
            .startsWith(true)
            .message("Executing Gradle on JVM versions 16 and lower has been deprecated")
            .stackTracePrefix("org.gradle.launcher.")
            .build(),
        SuppressedMessage.builder()
            .message("The resolvable usage is already allowed on configuration")
            .stackTracePrefix("org.jetbrains.kotlin.gradle.plugin.")
            .build(),
        SuppressedMessage.builder()
            .message("The DefaultSourceDirectorySet constructor has been deprecated")
            .stackTracePrefix("org.jetbrains.kotlin.gradle.plugin.")
            .build(),
        SuppressedMessage.builder()
            .message("Classpath configuration has been deprecated for dependency declaration")
            .stackTracePrefix("org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinCompilation.")
            .build(),
        SuppressedMessage.builder()
            .message("Internal API constructor TaskReportContainer(Class<T>, Task) has been deprecated")
            .stackTracePrefix("com.github.spotbugs.")
            .build(),
        SuppressedMessage.builder()
            .message("BuildListener#buildStarted(Gradle) has been deprecated")
            .stackTracePrefix("org.jetbrains.gradle.ext.")
            .build(),
        SuppressedMessage.builder()
            .message("org.gradle.util.ConfigureUtil type has been deprecated")
            .stackTracePrefix("io.github.gradlenexus.publishplugin.")
            .build()
    );

    @Getter(NONE)
    private final List<SuppressedMessage> suppressedDeprecationMessages =
        new ArrayList<>(DEFAULT_SUPPRESSED_DEPRECATIONS_MESSAGES);


    private static final List<String> DEFAULT_MUTABLE_PROJECT_STATE_WARNINGS = List.of(
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


    private static final List<String> DEFAULT_OPTIMIZATIONS_DISABLED_WARNINGS = List.of(
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

    public void addForbiddenMessage(String message) {
        forbiddenMessages.add(message);
    }

    public void addSuppressedForbiddenMessage(SuppressedMessage suppressedMessage) {
        suppressedForbiddenMessages.add(suppressedMessage);
    }

    public void addDeprecationMessage(String message) {
        deprecationMessages.add(message);
    }

    public void addSuppressedDeprecationMessage(SuppressedMessage suppressedMessage) {
        suppressedDeprecationMessages.add(suppressedMessage);
    }

    public void addMutableProjectStateWarning(String message) {
        mutableProjectStateWarnings.add(message);
    }

    public void addSuppressedMutableProjectStateWarning(
        SuppressedMessage suppressedMessage
    ) {
        suppressedMutableProjectStateWarnings.add(suppressedMessage);
    }

    public void addOptimizationsDisabledWarning(String message) {
        optimizationsDisabledWarnings.add(message);
    }

    public void addSuppressedOptimizationsDisabledWarning(
        SuppressedMessage suppressedMessage
    ) {
        suppressedOptimizationsDisabledWarnings.add(suppressedMessage);
    }


    private boolean withPluginClasspath = true;

    public void withoutPluginClasspath() {
        withPluginClasspath = false;
    }


    private boolean withConfigurationCache =
        isCurrentGradleVersionGreaterThanOrEqualTo(MIN_GRADLE_VERSION_WITH_CONFIGURATION_CACHE);

    public void withoutConfigurationCache() {
        withConfigurationCache = false;
    }

    private boolean withJacoco = currentJvmArgsHaveJacocoJvmArg();

    public void withoutJacoco() {
        withJacoco = false;
    }

    public void cleanBuildDir() {
        logger.lifecycle("Deleting the build dir...");
        deleteRecursively(getProjectDir().toPath().resolve("build"));
    }

    public final BuildResult assertBuildSuccessfully(String argument, String... otherArguments) {
        var arguments = concatArguments(new String[]{argument}, otherArguments);
        return build(arguments, true);
    }

    public final BuildResult assertBuildFails(String argument, String... otherArguments) {
        var arguments = concatArguments(new String[]{argument}, otherArguments);
        return build(arguments, false);
    }

    @SneakyThrows
    @SuppressWarnings({"java:S106", "java:S3776", "UnstableApiUsage"})
    private BuildResult build(String[] arguments, boolean isExpectingSuccess) {
        if (isExpectingSuccess) {
            logger.lifecycle("Building (expecting success)...");
        } else {
            logger.lifecycle("Building (expecting failure)...");
        }

        if (withJacoco) {
            injectJacocoDumper();
        }

        writeToDisk();

        File jacocoProjectDir = null;
        if (withJacoco && withConfigurationCache) {
            jacocoProjectDir = new File(
                projectDir.getParentFile(),
                projectDir.getName() + ".jacoco"
            );
            if (!jacocoProjectDir.isDirectory()) {
                copyRecursively(projectDir.toPath(), jacocoProjectDir.toPath());
            }
        }


        var runner = createGradleRunner(projectDir, withConfigurationCache, arguments);
        if (withJacoco && jacocoProjectDir == null) {
            injectJacocoArgs(runner);
        }

        final BuildResult buildResult;
        if (isExpectingSuccess) {
            buildResult = runner.build();
            if (withConfigurationCache) {
                logger.lifecycle("\nRebuilding to validate the Configuration Cache additionally...\n");
                /*
                 * Let's check that rebuild doesn't fail.
                 * It helps to validate the Configuration Cache additionally.
                 */
                runner.build();
            }
        } else {
            buildResult = runner.buildAndFail();
        }

        var output = buildResult.getOutput();
        List<String> outputLines = Splitter.onPattern("[\\n\\r]+").splitToStream(output)
            .map(StringUtils::trimRight)
            .filter(not(String::isEmpty))
            .collect(toUnmodifiableList());
        assertNoForbiddenMessages(outputLines);
        assertNoDeprecationMessages(outputLines);
        assertNoMutableProjectStateWarnings(outputLines);
        assertNoOptimizationsDisabledWarnings(outputLines);


        if (jacocoProjectDir != null) {
            logger.lifecycle("\nBuilding without the Configuration Cache to collect test coverage"
                + " (see https://github.com/gradle/gradle/issues/25979)...\n");
            var jacocoRunner = createGradleRunner(jacocoProjectDir, false, arguments);
            injectJacocoArgs(jacocoRunner);
            if (isExpectingSuccess) {
                jacocoRunner.build();
            } else {
                jacocoRunner.buildAndFail();
            }
        }
        return buildResult;
    }

    private void assertNoForbiddenMessages(List<String> outputLines) {
        var errors = parseErrors(
            outputLines,
            forbiddenMessages,
            suppressedForbiddenMessages,
            null
        );
        if (!errors.isEmpty()) {
            var sb = new StringBuilder();
            sb.append("Forbidden warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    private void assertNoDeprecationMessages(List<String> outputLines) {
        var errors = parseErrors(
            outputLines,
            deprecationMessages,
            suppressedDeprecationMessages,
            null
        );
        if (!errors.isEmpty()) {
            var sb = new StringBuilder();
            sb.append("Deprecation warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    private void assertNoMutableProjectStateWarnings(List<String> outputLines) {
        var errors = parseErrors(
            outputLines,
            mutableProjectStateWarnings,
            suppressedMutableProjectStateWarnings,
            null
        );
        if (!errors.isEmpty()) {
            var sb = new StringBuilder();
            sb.append("Mutable Project State warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    private void assertNoOptimizationsDisabledWarnings(List<String> outputLines) {
        var errors = parseErrors(
            outputLines,
            optimizationsDisabledWarnings,
            suppressedOptimizationsDisabledWarnings,
            null
        );
        if (!errors.isEmpty()) {
            var sb = new StringBuilder();
            sb.append("Optimizations disabled warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
    }

    private static final Pattern STACK_TRACE_LINE = Pattern.compile("^\\s+at ");

    @SuppressWarnings({"java:S3776", "java:S127"})
    private Collection<String> parseErrors(
        List<String> outputLines,
        List<String> messages,
        List<SuppressedMessage> suppressedMessages,
        @Nullable Predicate<String> isWarningEndLine
    ) {
        var currentBaseGradleVersion = GradleVersion.current().getBaseVersion();
        suppressedMessages = suppressedMessages.stream()
            .filter(msg -> msg.getMinGradleVersion() == null
                || msg.getMinGradleVersion().getBaseVersion().compareTo(currentBaseGradleVersion) <= 0
            )
            .filter(msg -> msg.getMaxGradleVersion() == null
                || msg.getMaxGradleVersion().getBaseVersion().compareTo(currentBaseGradleVersion) >= 0
            )
            .collect(toUnmodifiableList());

        Collection<String> errors = new LinkedHashSet<>();
        forEachLine:
        for (int lineIndex = 0; lineIndex < outputLines.size(); ++lineIndex) {
            var line = outputLines.get(lineIndex);
            var hasError = messages.stream().anyMatch(line::contains);
            if (!hasError) {
                continue;
            }

            for (var suppressedMessage : suppressedMessages) {
                boolean matches;
                if (suppressedMessage.isStartsWith()) {
                    matches = line.startsWith(suppressedMessage.getMessage());
                } else {
                    matches = line.contains(suppressedMessage.getMessage());
                }
                if (matches) {
                    var stackTracePackagePrefix = suppressedMessage.getStackTracePrefix();
                    if (isEmpty(stackTracePackagePrefix)) {
                        continue forEachLine;
                    }

                    for (int i = lineIndex + 1; i < outputLines.size(); ++i) {
                        var stackTraceLine = outputLines.get(i);
                        if (!STACK_TRACE_LINE.matcher(stackTraceLine).find()) {
                            break;
                        }
                        if (stackTraceLine.trim().startsWith("at " + stackTracePackagePrefix)) {
                            continue forEachLine;
                        }
                    }
                }
            }

            if (isWarningEndLine == null) {
                errors.add(line);
                continue;
            }

            var message = new StringBuilder();
            message.append(line);
            for (++lineIndex; lineIndex < outputLines.size(); ++lineIndex) {
                var nextLine = outputLines.get(lineIndex);
                message.append('\n').append(nextLine);
                if (isWarningEndLine.test(nextLine)) {
                    break;
                }
            }
            errors.add(message.toString());
        }

        return errors;
    }


    @SneakyThrows
    private GradleRunner createGradleRunner(File projectDir, boolean withConfigurationCache, String[] arguments) {
        var gradleJavaCompatibility = getGradleJavaCompatibility();
        if (gradleJavaCompatibility == UNSUPPORTED) {
            throw new AssertionError(format(
                "Gradle %s does NOT support Java %s",
                GradleVersion.current().getVersion(),
                JavaVersion.current().getMajorVersion()
            ));
        }

        var allArguments = concatArguments(
            new String[]{
                "--stacktrace",
                "--warning-mode=all",
                "-Dorg.gradle.parallel=true",
                "-Dorg.gradle.workers.max=4",
                "-Dorg.gradle.kotlin.dsl.allWarningsAsErrors=true",
                "-Dmaven.repo.local=" + new File(projectDir, ".m2-auto").getAbsolutePath(),
                "-Dorg.gradle.vfs.watch=false",
                "-Dorg.gradle.daemon=false",
                "-Dhttp.keepAlive=false",
                "-Dsun.net.http.retryPost=false",
                "-Dsun.io.useCanonCaches=false",
                "-Dsun.net.client.defaultConnectTimeout=15000",
                "-Dsun.net.client.defaultReadTimeout=300000",
                "-Djava.awt.headless=true",
                "-Dorg.gradle.dependency.verification.console=verbose",
                "-Dorg.gradle.daemon.idletimeout=10000",
                "-Dorg.gradle.daemon.performance.disable-logging=true",
                "-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false"
            },
            arguments
        );

        var runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .forwardOutput()
            //.withDebug(isDebugEnabled())
            .withArguments(allArguments);

        if (isCurrentGradleVersionGreaterThanOrEqualTo(MIN_GRADLE_VERSION_WITH_RUNNER_ENVIRONMENT)) {
            runner.withEnvironment(ImmutableMap.of(
                IS_IN_TEST_ENV_VAR, "true",
                IS_IN_FUNCTIONAL_TEST_ENV_VAR, "true"
            ));
        }

        if (withPluginClasspath) {
            runner.withPluginClasspath();
        }

        if (withConfigurationCache) {
            if (isConfigurationCacheSupportedWithAppliedPlugins()) {
                runner.withArguments(Stream.concat(
                    runner.getArguments().stream(),
                    Stream.of(
                        "--configuration-cache",
                        "--configuration-cache-problems=fail",
                        "-Dorg.gradle.configuration-cache.stable=true",
                        "-Dorg.gradle.configuration-cache.parallel=true",
                        "-Dorg.gradle.configuration-cache.integrity-check=true",
                        "-Dorg.gradle.unsafe.isolated-projects=true"
                    )
                ).collect(toUnmodifiableList()));
            }
        }

        String gradleDistribMirror = System.getenv("GRADLE_DISTRIBUTIONS_MIRROR");
        if (isEmpty(gradleDistribMirror)) {
            runner.withGradleVersion(GradleVersion.current().getVersion());
        } else {
            var distributionUri = new URI(trimRightWith(gradleDistribMirror, '/') + format(
                "/gradle-%s-bin.zip",
                GradleVersion.current().getVersion()
            ));
            runner.withGradleDistribution(distributionUri);
        }

        return runner;
    }

    private boolean isConfigurationCacheSupportedWithAppliedPlugins() {
        var allAppliedPlugins = Stream.of(
                getSettingsFile().getAppliedPlugins().stream(),
                getBuildFile().getAppliedPlugins().stream(),
                getChildren().values().stream().flatMap(project -> project.getBuildFile().getAppliedPlugins().stream())
            )
            .flatMap(identity())
            .distinct()
            .collect(toUnmodifiableList());
        if (allAppliedPlugins.isEmpty()) {
            return true;
        }

        return allAppliedPlugins.stream()
            .map(ConfigurationCacheUtils::getCorePluginConfigurationCacheSupport)
            .allMatch(SUPPORTED::equals);
    }

    private void injectJacocoArgs(GradleRunner runner) {
        var jacocoJvmArg = parseJacocoJvmArgFromCurrentJvmArgs();
        if (jacocoJvmArg == null) {
            return;
        }

        jacocoJvmArg.makePathsAbsolute();
        jacocoJvmArg.excludeGradleClasses();
        jacocoJvmArg.append(true);
        jacocoJvmArg.dumpOnExit(false);
        jacocoJvmArg.jmx(true);

        withJvmArguments(runner, jacocoJvmArg.toString());
    }

    private boolean jacocoDumperInjected;


    /**
     * See <a href="https://discuss.gradle.org/t/jacoco-gradle-test-kit-with-java/36603/9">https://discuss.gradle.org/t/jacoco-gradle-test-kit-with-java/36603/9</a>.
     */
    private void injectJacocoDumper() {
        if (jacocoDumperInjected) {
            return;
        } else {
            jacocoDumperInjected = true;
        }

        injectJacocoDumperImpl();
    }


    private static String[] concatArguments(String[]... argumentsArray) {
        return stream(argumentsArray)
            .filter(Objects::nonNull)
            .flatMap(Arrays::stream)
            .filter(Objects::nonNull)
            .toArray(String[]::new);
    }

}
