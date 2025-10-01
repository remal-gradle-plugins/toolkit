package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ActionUtils.doNothingAction;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyContent;
import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinContent;
import name.remal.gradle_plugins.toolkit.testkit.TestClasspath;
import org.gradle.api.Action;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleProjectTestTasks {

    public static <Block extends JavaLikeContent<Block>> void configureJunitTests(
        String sourceSetName,
        Block buildFileBlock,
        Action<? super Block> useJUnitPlatformConfigurer,
        Action<? super Block> taskConfigurer
    ) {
        buildFileBlock.block("dependencies", deps -> {
            var dependenciesMapping = ImmutableMap.of(
                sourceSetName + "Implementation", List.of(
                    "org.junit.jupiter:junit-jupiter-api"
                ),
                sourceSetName + "RuntimeOnly", List.of(
                    "org.junit.jupiter:junit-jupiter-engine",
                    "org.junit.platform:junit-platform-launcher"
                )
            );
            dependenciesMapping.forEach((configurationName, notations) -> {
                deps.line(
                    "%s(files(%s))",
                    configurationName,
                    notations.stream()
                        .map(TestClasspath::getTestClasspathLibraryFilePaths)
                        .flatMap(Collection::stream)
                        .map(Path::toString)
                        .distinct()
                        .map(path -> '"' + buildFileBlock.escapeString(path) + '"')
                        .collect(joining(", "))
                );
            });
        });

        final String taskConfigStmt;
        if (buildFileBlock instanceof GroovyContent) {
            taskConfigStmt = format("tasks.named(\"%s\", Test)", sourceSetName);
        } else if (buildFileBlock instanceof KotlinContent) {
            taskConfigStmt = format("tasks.named<Test>(\"%s\")", sourceSetName);
        } else {
            throw new UnsupportedOperationException();
        }
        buildFileBlock.block(taskConfigStmt, task -> {
            task.block("useJUnitPlatform", useJUnitPlatformConfigurer::execute); // TODO: simplify
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
