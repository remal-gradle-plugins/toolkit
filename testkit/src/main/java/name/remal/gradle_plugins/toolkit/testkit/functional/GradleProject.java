package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.NONE;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
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
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;
import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;
import static name.remal.gradle_plugins.toolkit.StringUtils.trimRightWith;
import static name.remal.gradle_plugins.toolkit.testkit.functional.GradleRunnerUtils.withJvmArguments;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.File;
import java.lang.management.ManagementFactory;
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
import javax.management.MBeanServer;
import javax.management.ObjectName;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.ConfigurationCacheUtils;
import name.remal.gradle_plugins.toolkit.StringUtils;
import name.remal.gradle_plugins.toolkit.generators.GroovyFileContent;
import org.gradle.api.JavaVersion;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.Contract;

@Getter
public class GradleProject extends AbstractGradleProject<GradleProject, BuildFile> {

    private static final Duration DEFAULT_TASK_TIMEOUT = Duration.ofMinutes(1);

    private static final GradleVersion MIN_GRADLE_VERSION_WITH_RUNNER_ENVIRONMENT = GradleVersion.version("5.2");
    private static final GradleVersion MIN_GRADLE_VERSION_WITH_CONFIGURATION_CACHE = GradleVersion.version("6.6");


    protected final Map<String, GradleChildProject> children = synchronizedMap(new LinkedHashMap<>());

    protected final SettingsFile settingsFile;

    public GradleProject(File projectDir) {
        super(projectDir);
        this.settingsFile = new SettingsFile(this.projectDir);
        setTaskTimeout(DEFAULT_TASK_TIMEOUT);
    }

    @Override
    protected BuildFile createBuildFile(File projectDir) {
        return new BuildFile(projectDir);
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject forSettingsFile(Consumer<SettingsFile> settingsFileConsumer) {
        settingsFileConsumer.accept(settingsFile);
        return getSelf();
    }

    @Override
    protected void writeToDisk() {
        super.writeToDisk();
        settingsFile.writeToDisk();

        children.values().forEach(AbstractGradleProject::writeToDisk);
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
        return getSelf();
    }


    @Getter(NONE)
    private final List<String> forbiddenMessages = new ArrayList<>();

    @Getter(NONE)
    private final List<SuppressedMessage> suppressedForbiddenMessages = new ArrayList<>();

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
    public final synchronized GradleProject addForbiddenMessage(String message) {
        assertIsNotBuilt();
        forbiddenMessages.add(message);
        return getSelf();
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addSuppressedForbiddenMessage(SuppressedMessage suppressedMessage) {
        assertIsNotBuilt();
        suppressedForbiddenMessages.add(suppressedMessage);
        return getSelf();
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addDeprecationMessage(String message) {
        assertIsNotBuilt();
        deprecationMessages.add(message);
        return getSelf();
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addSuppressedDeprecationMessage(SuppressedMessage suppressedMessage) {
        assertIsNotBuilt();
        suppressedDeprecationMessages.add(suppressedMessage);
        return getSelf();
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addMutableProjectStateWarning(String message) {
        assertIsNotBuilt();
        mutableProjectStateWarnings.add(message);
        return getSelf();
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addSuppressedMutableProjectStateWarning(
        SuppressedMessage suppressedMessage
    ) {
        assertIsNotBuilt();
        suppressedMutableProjectStateWarnings.add(suppressedMessage);
        return getSelf();
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addOptimizationsDisabledWarning(String message) {
        assertIsNotBuilt();
        optimizationsDisabledWarnings.add(message);
        return getSelf();
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject addSuppressedOptimizationsDisabledWarning(
        SuppressedMessage suppressedMessage
    ) {
        assertIsNotBuilt();
        suppressedOptimizationsDisabledWarnings.add(suppressedMessage);
        return getSelf();
    }


    private boolean withPluginClasspath = true;

    @Contract("-> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject withoutPluginClasspath() {
        assertIsNotBuilt();
        withPluginClasspath = false;
        return getSelf();
    }


    private boolean withConfigurationCache =
        isCurrentGradleVersionGreaterThanOrEqualTo(MIN_GRADLE_VERSION_WITH_CONFIGURATION_CACHE);

    @Contract("-> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject withoutConfigurationCache() {
        assertIsNotBuilt();
        withConfigurationCache = false;
        return getSelf();
    }

    @Nullable
    private Duration taskTimeout;

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject setTaskTimeout(@Nullable Duration timeout) {
        taskTimeout = timeout;
        buildFile.setTaskTimeout(taskTimeout);
        children.values().forEach(child -> {
            child.buildFile.setTaskTimeout(taskTimeout);
        });
        return getSelf();
    }

    private boolean withJacoco = currentJvmArgsHaveJacocoJvmArg();

    @Contract("-> this")
    @CanIgnoreReturnValue
    public final synchronized GradleProject withoutJacoco() {
        assertIsNotBuilt();
        withJacoco = false;
        return getSelf();
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
    @SuppressWarnings({"java:S106", "java:S3776", "UnstableApiUsage"})
    private synchronized BuildResult build(boolean isExpectingSuccess) {
        if (buildException != null) {
            throw buildException;
        }

        if (buildResult == null) {
            final BuildResult currentBuildResult;
            try {
                if (withJacoco) {
                    injectJacocoDumper();
                }


                writeToDisk();


                File jacocoProjectDir = null;
                if (withJacoco && withConfigurationCache) {
                    jacocoProjectDir = normalizeFile(new File(
                        projectDir.getParentFile(),
                        projectDir.getName() + ".jacoco"
                    ));
                    copyRecursively(projectDir.toPath(), jacocoProjectDir.toPath());
                }


                val runner = createGradleRunner(projectDir, withConfigurationCache);
                if (withJacoco && jacocoProjectDir == null) {
                    injectJacocoArgs(runner);
                }

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
                assertNoForbiddenMessages(outputLines);
                assertNoDeprecationMessages(outputLines);
                assertNoMutableProjectStateWarnings(outputLines);
                assertNoOptimizationsDisabledWarnings(outputLines);


                if (jacocoProjectDir != null) {
                    val jacocoRunner = createGradleRunner(jacocoProjectDir, false);
                    injectJacocoArgs(jacocoRunner);
                    if (isExpectingSuccess) {
                        jacocoRunner.build();
                    } else {
                        jacocoRunner.buildAndFail();
                    }
                }

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

    private void assertNoForbiddenMessages(List<String> outputLines) {
        val errors = parseErrors(
            outputLines,
            forbiddenMessages,
            suppressedForbiddenMessages,
            null
        );
        if (!errors.isEmpty()) {
            val sb = new StringBuilder();
            sb.append("Forbidden warnings were found:");
            errors.forEach(it -> sb.append("\n  * ").append(it));
            throw new AssertionError(sb.toString());
        }
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

    private static final Pattern STACK_TRACE_LINE = Pattern.compile("^\\s+at ");

    @SuppressWarnings({"java:S3776", "java:S127"})
    private Collection<String> parseErrors(
        List<String> outputLines,
        List<String> messages,
        List<SuppressedMessage> suppressedMessages,
        @Nullable Predicate<String> isWarningEndLine
    ) {
        val currentBaseGradleVersion = GradleVersion.current().getBaseVersion();
        suppressedMessages = suppressedMessages.stream()
            .filter(msg -> msg.getMinGradleVersion() == null
                || msg.getMinGradleVersion().getBaseVersion().compareTo(currentBaseGradleVersion) <= 0
            )
            .filter(msg -> msg.getMaxGradleVersion() == null
                || msg.getMaxGradleVersion().getBaseVersion().compareTo(currentBaseGradleVersion) >= 0
            )
            .collect(toList());

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
                    val stackTracePackagePrefix = suppressedMessage.getStackTracePrefix();
                    if (isEmpty(stackTracePackagePrefix)) {
                        continue forEachLine;
                    }

                    for (int i = lineIndex + 1; i < outputLines.size(); ++i) {
                        val stackTraceLine = outputLines.get(i);
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


    @SneakyThrows
    private GradleRunner createGradleRunner(File projectDir, boolean withConfigurationCache) {
        val gradleJavaCompatibility = getGradleJavaCompatibility();
        if (gradleJavaCompatibility == UNSUPPORTED) {
            throw new AssertionError(format(
                "Gradle %s does NOT support Java %s",
                GradleVersion.current().getVersion(),
                JavaVersion.current().getMajorVersion()
            ));
        }

        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .forwardOutput()
            //.withDebug(isDebugEnabled())
            .withArguments(
                "--stacktrace",
                "--warning-mode=all",
                "-Dorg.gradle.parallel=true",
                "-Dorg.gradle.workers.max=4",
                "-Dorg.gradle.kotlin.dsl.allWarningsAsErrors=true",
                "-Dmaven.repo.local=" + new File(projectDir, ".m2").getAbsolutePath(),
                "-Dorg.gradle.vfs.watch=false",
                "-Dorg.gradle.daemon=false",
                "-Dhttp.keepAlive=false",
                "-Dsun.net.http.retryPost=false",
                "-Dsun.io.useCanonCaches=false",
                "-Dsun.net.client.defaultConnectTimeout=15000",
                "-Dsun.net.client.defaultReadTimeout=300000",
                "-Djava.awt.headless=true",
                "-Dorg.gradle.dependency.verification.console=verbose",
                "-Dorg.gradle.daemon.performance.disable-logging=true",
                "-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false"
            );

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
                        "-Dorg.gradle.configuration-cache.stable=true"
                    )
                ).collect(toList()));
            }
        }

        String gradleDistribMirror = System.getenv("GRADLE_DISTRIBUTIONS_MIRROR");
        if (isEmpty(gradleDistribMirror)) {
            runner.withGradleVersion(GradleVersion.current().getVersion());
        } else {
            val distributionUri = new URI(trimRightWith(gradleDistribMirror, '/') + format(
                "/gradle-%s-bin.zip",
                GradleVersion.current().getVersion()
            ));
            runner.withGradleDistribution(distributionUri);
        }

        return runner;
    }

    private boolean isConfigurationCacheSupportedWithAppliedPlugins() {
        val allAppliedPlugins = Stream.of(
                getSettingsFile().getAppliedPlugins().stream(),
                getBuildFile().getAppliedPlugins().stream(),
                getChildren().values().stream().flatMap(project -> project.getBuildFile().getAppliedPlugins().stream())
            )
            .flatMap(identity())
            .distinct()
            .collect(toList());
        if (allAppliedPlugins.isEmpty()) {
            return true;
        }

        return allAppliedPlugins.stream()
            .map(ConfigurationCacheUtils::getCorePluginConfigurationCacheSupport)
            .allMatch(SUPPORTED::equals);
    }

    private void injectJacocoArgs(GradleRunner runner) {
        val jacocoJvmArg = parseJacocoJvmArgFromCurrentJvmArgs();
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

    /**
     * See <a href="https://discuss.gradle.org/t/jacoco-gradle-test-kit-with-java/36603/9">https://discuss.gradle.org/t/jacoco-gradle-test-kit-with-java/36603/9</a>.
     */
    private void injectJacocoDumper() {
        val settingsFile = getSettingsFile();
        settingsFile.addImport(MBeanServer.class);
        settingsFile.addImport(ManagementFactory.class);
        settingsFile.addImport(ObjectName.class);

        settingsFile.append(
            "\n",
            "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */",
            "// Jacoco dumper logic:"
        );

        Consumer<GroovyFileContent> jacocoBumperLogicBlock = rootBlock -> {
            rootBlock.append("MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer()");
            rootBlock.append("ObjectName jacocoObjectName = ObjectName.getInstance(\"org.jacoco:type=Runtime\")");
            rootBlock.appendBlock("if (mbeanServer.isRegistered(jacocoObjectName))", ifBlock -> {
                //ifBlock.append("println '!!! dumping jacoco data !!!'");
                ifBlock.append(
                    "mbeanServer.invoke(",
                    "    jacocoObjectName,",
                    "    \"dump\",",
                    "    [ true ].toArray(new Object[0]),",
                    "    [ \"boolean\" ].toArray(new String[0])",
                    ")"
                );
                //ifBlock.append("println '!!! dumped jacoco data !!!'");
            });
        };

        if (isCurrentGradleVersionGreaterThanOrEqualTo("6.1")) {
            settingsFile.addImport("org.gradle.api.services.BuildService");
            settingsFile.addImport("org.gradle.api.services.BuildServiceParameters");

            settingsFile.appendBlock(
                "abstract class JacocoDumper implements BuildService<BuildServiceParameters.None>, AutoCloseable",
                classBlock -> {
                    classBlock.appendBlock("void close()", jacocoBumperLogicBlock);
                }
            );

            settingsFile.append(
                "gradle.sharedServices.registerIfAbsent(",
                "    \"" + escapeGroovy(GradleProject.class.getName() + ":jacocoDumper") + "\",",
                "    JacocoDumper,",
                "    { }",
                ").get()"
            );

        } else {
            settingsFile.appendBlock("gradle.buildFinished", jacocoBumperLogicBlock);
        }
    }

}
