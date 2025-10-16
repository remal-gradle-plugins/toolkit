package name.remal.gradle_plugins.toolkit.testkit.functional;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.NONE;
import static name.remal.gradle_plugins.toolkit.ConfigurationCacheUtils.getPluginConfigurationCacheSupport;
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
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.toolkit.StringUtils;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleBuildFileContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleSettingsFileContent;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.UnexpectedBuildResultException;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.Unmodifiable;
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


    private static final boolean IS_CONFIGURATION_CACHE_SUPPORTED = isCurrentGradleVersionGreaterThanOrEqualTo("6.6");

    private static final boolean ARE_ISOLATED_PROJECTS_SUPPORTED = isCurrentGradleVersionGreaterThanOrEqualTo("8.5");

    private static final boolean IS_RUNNER_ENVIRONMENT_SUPPORTED = isCurrentGradleVersionGreaterThanOrEqualTo("5.2");


    protected final File mavenLocalRepoDir = new File(projectDir, ".m2");

    protected final Map<String, Child> children = new LinkedHashMap<>();

    protected final SettingsFileType settingsFile;

    protected AbstractGradleProject(File projectDir) {
        super(projectDir);
        this.settingsFile = createSettingsFileContent();
    }


    public final void forSettingsFile(Action<? super SettingsFileType> action) {
        action.execute(settingsFile);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void writeToDisk() {
        int maxWorkers = max(2, (int) floor(0.75 * Runtime.getRuntime().availableProcessors()));

        putGradlePropertyIfAbsent("org.gradle.parallel", true);
        putGradlePropertyIfAbsent("org.gradle.workers.max", maxWorkers);
        putGradlePropertyIfAbsent("org.gradle.daemon.idletimeout", 5_000);
        putGradlePropertyIfAbsent("org.gradle.vfs.watch", false);
        putGradlePropertyIfAbsent("org.gradle.kotlin.dsl.allWarningsAsErrors", true);
        putGradlePropertyIfAbsent("org.gradle.dependency.verification", "strict");
        putGradlePropertyIfAbsent("org.gradle.dependency.verification.console", "verbose");
        putGradlePropertyIfAbsent("org.gradle.internal.launcher.welcomeMessageEnabled", false);

        putGradlePropertyIfAbsent("systemProp.maven.repo.local", mavenLocalRepoDir);


        super.writeToDisk();


        writeTextFile(getSettingsFileName(), settingsFile.toString());
        children.values().forEach(AbstractBaseGradleProject::writeToDisk);
    }

    public final Child newChildProject(String name) {
        return children.computeIfAbsent(name, __ -> {
            var childProjectDir = new File(projectDir, name);
            var child = createChildProject(childProjectDir);
            settingsFile.line("include(\":%s\")", settingsFile.escapeString(child.getName()));
            return child;
        });
    }

    public final Child newChildProject(String name, Action<? super Child> action) {
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


    private static final List<String> DEFAULT_TASK_CONFIGURATION_WARNINGS = List.of(
        "A problem was found with the configuration of task "
    );

    @Getter(NONE)
    private final List<String> taskConfigurationWarnings =
        new ArrayList<>(DEFAULT_TASK_CONFIGURATION_WARNINGS);

    private static final List<SuppressedMessage> DEFAULT_SUPPRESSED_TASK_CONFIGURATION_WARNINGS =
        emptyList();

    @Getter(NONE)
    private final List<SuppressedMessage> suppressedTaskConfigurationWarnings =
        new ArrayList<>(DEFAULT_SUPPRESSED_TASK_CONFIGURATION_WARNINGS);


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

    public final void addForbiddenMessage(String message) {
        forbiddenMessages.add(message);
    }

    public final void addSuppressedForbiddenMessage(SuppressedMessage suppressedMessage) {
        suppressedForbiddenMessages.add(suppressedMessage);
    }

    public final void addDeprecationMessage(String message) {
        deprecationMessages.add(message);
    }

    public final void addSuppressedDeprecationMessage(SuppressedMessage suppressedMessage) {
        suppressedDeprecationMessages.add(suppressedMessage);
    }

    public final void addTaskConfigurationWarnings(String message) {
        taskConfigurationWarnings.add(message);
    }

    public final void addMutableProjectStateWarning(String message) {
        mutableProjectStateWarnings.add(message);
    }

    public final void addSuppressedMutableProjectStateWarning(
        SuppressedMessage suppressedMessage
    ) {
        suppressedMutableProjectStateWarnings.add(suppressedMessage);
    }

    public final void addOptimizationsDisabledWarning(String message) {
        optimizationsDisabledWarnings.add(message);
    }

    public final void addSuppressedOptimizationsDisabledWarning(
        SuppressedMessage suppressedMessage
    ) {
        suppressedOptimizationsDisabledWarnings.add(suppressedMessage);
    }


    private boolean withPluginClasspath = true;

    public final void withoutPluginClasspath() {
        withPluginClasspath = false;
    }

    private boolean withConfigurationCache = IS_CONFIGURATION_CACHE_SUPPORTED;

    public final void withoutConfigurationCache() {
        withConfigurationCache = false;
    }

    private boolean withIsolatedProjects = withConfigurationCache && ARE_ISOLATED_PROJECTS_SUPPORTED;

    public final void withoutIsolatedProjects() {
        withIsolatedProjects = false;
    }

    private boolean withJacoco = currentJvmArgsHaveJacocoJvmArg();

    public final void withoutJacoco() {
        withJacoco = false;
    }

    @OverridingMethodsMustInvokeSuper
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
    @SuppressWarnings({"java:S106", "java:S3776", "java:S2583", "java:S6541"})
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


        var withConfigurationCache = this.withConfigurationCache;
        var pluginsWithoutConfigurationCacheSupport = getAppliedPluginsThatDoNotSupportConfigurationCache();
        if (!pluginsWithoutConfigurationCacheSupport.isEmpty()) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                    "Configuration Cache testing was disabled"
                        + " because some of the applied plugins knowingly don't support it: {}",
                    join(", ", pluginsWithoutConfigurationCacheSupport)
                );
            }
            withConfigurationCache = false;
        }


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
        assertNoTaskConfigurationWarnings(outputLines);
        assertNoMutableProjectStateWarnings(outputLines);
        assertNoOptimizationsDisabledWarnings(outputLines);


        if (jacocoProjectDir != null) {
            logger.lifecycle("\nBuilding without the Configuration Cache to collect test coverage"
                + " (see https://github.com/gradle/gradle/issues/25979)...\n");
            var jacocoRunner = createGradleRunner(jacocoProjectDir, false, arguments);
            injectJacocoArgs(jacocoRunner);
            try {
                if (isExpectingSuccess) {
                    jacocoRunner.build();
                } else {
                    jacocoRunner.buildAndFail();
                }
            } catch (UnexpectedBuildResultException exception) {
                if (isExpectingSuccess) {
                    throw exception;
                } else {
                    // do nothing, because the build could fail due to Configuration Cache problems
                }
            }
        }


        return buildResult;
    }

    @Unmodifiable
    private Set<String> getAppliedPluginsThatDoNotSupportConfigurationCache() {
        return Stream.of(
                getSettingsFile().getAppliedPlugins().stream(),
                getBuildFile().getAppliedPlugins().stream(),
                getChildren().values().stream().flatMap(project -> project.getBuildFile().getAppliedPlugins().stream())
            )
            .flatMap(identity())
            .filter(pluginId -> getPluginConfigurationCacheSupport(pluginId) == UNSUPPORTED)
            .collect(toImmutableSet());
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

    private void assertNoTaskConfigurationWarnings(List<String> outputLines) {
        var errors = parseErrors(
            outputLines,
            taskConfigurationWarnings,
            suppressedTaskConfigurationWarnings,
            null
        );
        if (!errors.isEmpty()) {
            var sb = new StringBuilder();
            sb.append("Task configuration warnings were found:");
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
    @SuppressWarnings("java:S2259")
    private GradleRunner createGradleRunner(
        File projectDir,
        boolean withConfigurationCache,
        @Nullable String[] arguments
    ) {
        var gradleJavaCompatibility = getGradleJavaCompatibility();
        if (gradleJavaCompatibility == UNSUPPORTED) {
            throw new AssertionError(format(
                "Gradle %s does NOT support Java %s",
                GradleVersion.current().getVersion(),
                JavaVersion.current().getMajorVersion()
            ));
        }

        var allArguments = concatArguments(
            new @Nullable String[]{
                "--stacktrace",
                "--warning-mode=all",
            },
            arguments
        );

        if (withConfigurationCache) {
            allArguments = concatArguments(
                allArguments,
                new @Nullable String[]{
                    "--configuration-cache",
                    "--configuration-cache-problems=fail",
                    // TODO: add test (https://github.com/remal-gradle-plugins/toolkit/issues/1017):
                    "-Dorg.gradle.configuration-cache.stable=true",
                    // TODO: add test (https://github.com/remal-gradle-plugins/toolkit/issues/1018):
                    "-Dorg.gradle.configuration-cache.parallel=true",
                    "-Dorg.gradle.configuration-cache.integrity-check=true",
                    withIsolatedProjects ? "-Dorg.gradle.unsafe.isolated-projects=true" : null,
                }
            );
        }

        var runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .forwardOutput()
            //.withDebug(isDebugEnabled())
            .withArguments(allArguments);

        if (IS_RUNNER_ENVIRONMENT_SUPPORTED) {
            runner.withEnvironment(ImmutableMap.of(
                IS_IN_TEST_ENV_VAR, "true",
                IS_IN_FUNCTIONAL_TEST_ENV_VAR, "true"
            ));
        }

        if (withPluginClasspath) {
            runner.withPluginClasspath();
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


    private static String[] concatArguments(@Nullable String @Nullable []... argumentsArray) {
        if (argumentsArray == null) {
            return new String[0];
        }

        return stream(argumentsArray)
            .filter(Objects::nonNull)
            .flatMap(Arrays::stream)
            .filter(Objects::nonNull)
            .toArray(String[]::new);
    }

}
