package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.writeString;
import static name.remal.gradle_plugins.toolkit.ArchiveUtils.newEmptyZipArchive;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;

import com.google.errorprone.annotations.ForOverride;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;

abstract class SourceSetUtilsTestBase {

    @ForOverride
    protected boolean useMavenCentral() {
        return false;
    }

    final Project project;
    final TaskContainer tasks;
    final ConfigurationContainer configurations;
    final DependencyHandler dependencies;
    final Path generalDependencyFile;
    final SourceSetContainer sourceSets;
    final SourceSet mainSourceSet;
    final Path mainDependencyFile;
    final SourceSet testSourceSet;
    final Path testDependencyFile;

    @SneakyThrows
    protected SourceSetUtilsTestBase(Project project) {
        this.project = project;
        this.tasks = project.getTasks();
        this.configurations = project.getConfigurations();
        this.dependencies = project.getDependencies();
        final var projectDir = project.getProjectDir().toPath();


        project.getPluginManager().apply("java");

        if (useMavenCentral()) {
            project.getRepositories().mavenCentral();
        } else {
            project.getConfigurations().all(conf -> {
                var deps = conf.getDependencies();
                deps.withType(ExternalDependency.class).all(deps::remove);
            });
        }

        this.generalDependencyFile = newEmptyZipArchive(projectDir.resolve("dependency.jar"));
        this.sourceSets = project.getExtensions().getByType(SourceSetContainer.class);


        this.mainSourceSet = sourceSets.getByName(MAIN_SOURCE_SET_NAME);

        mainSourceSet.setCompileClasspath(mainSourceSet.getCompileClasspath().plus(project.files(
            mainDependencyFile = newEmptyZipArchive(projectDir.resolve("main-dependency.jar"))
        )));

        writeString(
            createParentDirectories(projectDir.resolve("src/main/java/MainJavaClass.java")),
            "class MainJavaClass {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/main/groovy/MainGroovyClass.groovy")),
            "class MainGroovyClass {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/main/kotlin/MainKotlinClass.kt")),
            "class MainKotlinClass {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/main/resources/main-resource.txt")),
            "main resource"
        );


        this.testSourceSet = sourceSets.getByName(TEST_SOURCE_SET_NAME);

        testSourceSet.setCompileClasspath(testSourceSet.getCompileClasspath().plus(project.files(
            testDependencyFile = newEmptyZipArchive(projectDir.resolve("test-dependency.jar"))
        )));

        writeString(
            createParentDirectories(projectDir.resolve("src/test/java/MainJavaClassTest.java")),
            "class javaMainJavaClassTest {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/test/groovy/MainGroovyClassTest.groovy")),
            "class MainGroovyClassTest {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/test/kotlin/MainKotlinClassTest.kt")),
            "class MainKotlinClassTest {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/test/resources/main-resource.txt")),
            "test resource"
        );
    }

}
