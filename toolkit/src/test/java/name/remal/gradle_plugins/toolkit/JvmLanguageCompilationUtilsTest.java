package name.remal.gradle_plugins.toolkit;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;
import org.junit.jupiter.api.Test;

class JvmLanguageCompilationUtilsTest extends SourceSetUtilsTestBase {

    JvmLanguageCompilationUtilsTest(Project project) {
        super(project);
    }

    @Test
    void configureJvmLanguageCompileTasks() {
        var destinationDirectories = new LinkedHashSet<File>();
        JvmLanguageCompilationUtils.configureJvmLanguageCompileTasks(project.getTasks(), (task, properties) -> {
            destinationDirectories.add(properties.getDestinationDirectory().getAsFile().get());
        });

        project.getTasks().forEach(task -> {
            // execute all configureEach()
        });


        assertThat(destinationDirectories).anySatisfy(dir -> {
            assertThat(dir.getAbsolutePath().replace(File.separatorChar, '/'))
                .endsWith("/build/classes/java/main");
        });


        var allClassesDirs = sourceSets.stream()
            .map(SourceSet::getOutput)
            .map(SourceSetOutput::getClassesDirs)
            .map(FileCollection::getFiles)
            .flatMap(Collection::stream)
            .distinct()
            .collect(toUnmodifiableList());

        assertThat(destinationDirectories)
            .containsExactlyInAnyOrderElementsOf(allClassesDirs);
    }

    @Test
    void configureJvmLanguageCompileTasks_groovy() {
        project.getPluginManager().apply("groovy");

        var destinationDirectories = new LinkedHashSet<File>();
        JvmLanguageCompilationUtils.configureJvmLanguageCompileTasks(project.getTasks(), (task, properties) -> {
            destinationDirectories.add(properties.getDestinationDirectory().getAsFile().get());
        });

        project.getTasks().forEach(task -> {
            // execute all configureEach()
        });


        assertThat(destinationDirectories).anySatisfy(dir -> {
            assertThat(dir.getAbsolutePath().replace(File.separatorChar, '/'))
                .endsWith("/build/classes/groovy/main");
        });


        var allClassesDirs = sourceSets.stream()
            .map(SourceSet::getOutput)
            .map(SourceSetOutput::getClassesDirs)
            .map(FileCollection::getFiles)
            .flatMap(Collection::stream)
            .distinct()
            .collect(toUnmodifiableList());

        assertThat(destinationDirectories)
            .containsExactlyInAnyOrderElementsOf(allClassesDirs);
    }

    @Test
    @TagKotlinPlugin
    void configureJvmLanguageCompileTasks_kotlin() {
        project.getPluginManager().apply("org.jetbrains.kotlin.jvm");

        var destinationDirectories = new LinkedHashSet<File>();
        JvmLanguageCompilationUtils.configureJvmLanguageCompileTasks(project.getTasks(), (task, properties) -> {
            destinationDirectories.add(properties.getDestinationDirectory().getAsFile().get());
        });

        project.getTasks().forEach(task -> {
            // execute all configureEach()
        });


        assertThat(destinationDirectories).anySatisfy(dir -> {
            assertThat(dir.getAbsolutePath().replace(File.separatorChar, '/'))
                .endsWith("/build/classes/kotlin/main");
        });


        var allClassesDirs = sourceSets.stream()
            .map(SourceSet::getOutput)
            .map(SourceSetOutput::getClassesDirs)
            .map(FileCollection::getFiles)
            .flatMap(Collection::stream)
            .distinct()
            .collect(toUnmodifiableList());

        assertThat(destinationDirectories)
            .containsExactlyInAnyOrderElementsOf(allClassesDirs);
    }

}
