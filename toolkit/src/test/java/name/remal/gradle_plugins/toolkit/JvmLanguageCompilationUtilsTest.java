package name.remal.gradle_plugins.toolkit;

import static java.util.stream.Collectors.toUnmodifiableList;
import static name.remal.gradle_plugins.toolkit.JvmLanguageCompilationUtils.configureJvmLanguageCompileTasks;
import static name.remal.gradle_plugins.toolkit.JvmLanguageCompilationUtils.isJvmLanguageCompileTask;
import static name.remal.gradle_plugins.toolkit.testkit.ProjectValidations.executeAfterEvaluateActions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JvmLanguageCompilationUtilsTest extends SourceSetUtilsTestBase {

    JvmLanguageCompilationUtilsTest(Project project) {
        super(project);
    }

    @Nested
    class IsJvmLanguageCompileTask {

        @Test
        void notCompilationTask() {
            assertFalse(isJvmLanguageCompileTask(tasks.named(mainSourceSet.getProcessResourcesTaskName()).get()));
        }

        @Test
        void java() {
            assertTrue(isJvmLanguageCompileTask(tasks.named(mainSourceSet.getCompileJavaTaskName()).get()));
        }

        @Test
        void groovy() {
            project.getPluginManager().apply("groovy");

            assertTrue(isJvmLanguageCompileTask(tasks.named(mainSourceSet.getCompileTaskName("groovy")).get()));
        }

        @Test
        @TagKotlinPlugin
        void kotlin() {
            project.getPluginManager().apply("org.jetbrains.kotlin.jvm");
            executeAfterEvaluateActions(project); // old Kotlin plugins configure a lot of things on afterEvaluate()

            assertTrue(isJvmLanguageCompileTask(tasks.named(mainSourceSet.getCompileTaskName("kotlin")).get()));
        }

    }


    @Nested
    class ConfigureJvmLanguageCompileTasks {

        @Test
        void java() {
            var destinationDirectories = new LinkedHashSet<File>();
            configureJvmLanguageCompileTasks(tasks, (task, properties) -> {
                destinationDirectories.add(properties.getDestinationDirectory().getAsFile().get());
            });

            tasks.forEach(task -> {
                // execute all configureEach()
            });


            assertThat(destinationDirectories).anySatisfy(dir -> {
                assertThat(dir.getAbsolutePath().replace(File.separatorChar, '/')).endsWith("/build/classes/java/main");
            });


            var allClassesDirs = sourceSets.stream()
                .map(SourceSet::getOutput)
                .map(SourceSetOutput::getClassesDirs)
                .map(FileCollection::getFiles)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toUnmodifiableList());

            assertThat(destinationDirectories).containsExactlyInAnyOrderElementsOf(allClassesDirs);
        }

        @Test
        void groovy() {
            project.getPluginManager().apply("groovy");

            var destinationDirectories = new LinkedHashSet<File>();
            configureJvmLanguageCompileTasks(tasks, (task, properties) -> {
                destinationDirectories.add(properties.getDestinationDirectory().getAsFile().get());
            });

            tasks.forEach(task -> {
                // execute all configureEach()
            });


            assertThat(destinationDirectories).anySatisfy(dir -> {
                assertThat(dir.getAbsolutePath()
                    .replace(File.separatorChar, '/')).endsWith("/build/classes/groovy/main");
            });


            var allClassesDirs = sourceSets.stream()
                .map(SourceSet::getOutput)
                .map(SourceSetOutput::getClassesDirs)
                .map(FileCollection::getFiles)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toUnmodifiableList());

            assertThat(destinationDirectories).containsExactlyInAnyOrderElementsOf(allClassesDirs);
        }

        @Test
        @TagKotlinPlugin
        void kotlin() {
            project.getPluginManager().apply("org.jetbrains.kotlin.jvm");
            executeAfterEvaluateActions(project); // old Kotlin plugins configure a lot of things on afterEvaluate()

            var destinationDirectories = new LinkedHashSet<File>();
            configureJvmLanguageCompileTasks(tasks, (task, properties) -> {
                destinationDirectories.add(properties.getDestinationDirectory().getAsFile().get());
            });

            tasks.forEach(task -> {
                // execute all configureEach()
            });


            assertThat(destinationDirectories).anySatisfy(dir -> {
                assertThat(dir.getAbsolutePath()
                    .replace(File.separatorChar, '/')).endsWith("/build/classes/kotlin/main");
            });


            var allClassesDirs = sourceSets.stream()
                .map(SourceSet::getOutput)
                .map(SourceSetOutput::getClassesDirs)
                .map(FileCollection::getFiles)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toUnmodifiableList());

            assertThat(destinationDirectories).containsExactlyInAnyOrderElementsOf(allClassesDirs);
        }

    }

}
