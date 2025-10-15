package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ActionUtils.doNothingAction;
import static name.remal.gradle_plugins.toolkit.testkit.functional.GradleProjectDependencies.addLibrariesAsConfigurationDependency;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;

import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyContent;
import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinContent;
import org.gradle.api.Action;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleProjectTestTasks {

    public static <Block extends JavaLikeContent<Block>> void configureJunitTests(
        String sourceSetName,
        Block buildFileBlock,
        Action<? super Block> useJUnitPlatformConfigurer,
        Action<? super Block> taskConfigurer
    ) {
        addLibrariesAsConfigurationDependency(
            buildFileBlock,
            sourceSetName + "Implementation",
            "org.junit.jupiter:junit-jupiter-api"
        );

        addLibrariesAsConfigurationDependency(
            buildFileBlock,
            sourceSetName + "RuntimeOnly",
            "org.junit.jupiter:junit-jupiter-engine",
            "org.junit.platform:junit-platform-launcher"
        );

        final String taskConfigStmt;
        if (buildFileBlock instanceof GroovyContent) {
            taskConfigStmt = format("tasks.named(\"%s\", Test)", sourceSetName);
        } else if (buildFileBlock instanceof KotlinContent) {
            taskConfigStmt = format("tasks.named<Test>(\"%s\")", sourceSetName);
        } else {
            throw new UnsupportedOperationException();
        }
        buildFileBlock.block(taskConfigStmt, task -> {
            task.block("useJUnitPlatform", useJUnitPlatformConfigurer);
            task.line("enableAssertions = true");
            task.block("testLogging", logging -> {
                logging.line("showExceptions = true");
                logging.line("showCauses = true");
                logging.line("showStackTraces = true");
                logging.line("setExceptionFormat(\"FULL\")");
                logging.line("stackTraceFilters(\"GROOVY\")");
                logging.line("events(\"PASSED\", \"SKIPPED\", \"FAILED\")");
            });
            taskConfigurer.execute(task);
        });
    }

    public static <Block extends JavaLikeContent<Block>> void configureJunitTests(
        Block buildFileBlock,
        Action<? super Block> useJUnitPlatformConfigurer,
        Action<? super Block> taskConfigurer
    ) {
        configureJunitTests(
            TEST_SOURCE_SET_NAME,
            buildFileBlock,
            useJUnitPlatformConfigurer,
            taskConfigurer
        );
    }

    public static <Block extends JavaLikeContent<Block>> void configureJunitTests(
        String sourceSetName,
        Block buildFileBlock,
        Action<? super Block> useJUnitPlatformConfigurer
    ) {
        configureJunitTests(
            sourceSetName,
            buildFileBlock,
            useJUnitPlatformConfigurer,
            doNothingAction()
        );
    }

    public static <Block extends JavaLikeContent<Block>> void configureJunitTests(
        Block buildFileBlock,
        Action<? super Block> useJUnitPlatformConfigurer
    ) {
        configureJunitTests(
            TEST_SOURCE_SET_NAME,
            buildFileBlock,
            useJUnitPlatformConfigurer
        );
    }

    public static <Block extends JavaLikeContent<Block>> void configureJunitTests(
        String sourceSetName,
        Block buildFileBlock
    ) {
        configureJunitTests(
            sourceSetName,
            buildFileBlock,
            doNothingAction()
        );
    }

    public static <Block extends JavaLikeContent<Block>> void configureJunitTests(
        Block buildFileBlock
    ) {
        configureJunitTests(
            TEST_SOURCE_SET_NAME,
            buildFileBlock
        );
    }

}
