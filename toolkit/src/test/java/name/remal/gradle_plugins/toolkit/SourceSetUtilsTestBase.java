package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.writeString;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;

import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

abstract class SourceSetUtilsTestBase {

    final Project project;
    final SourceSetContainer sourceSets;
    final SourceSet mainSourceSet;
    final SourceSet testSourceSet;

    @SneakyThrows
    protected SourceSetUtilsTestBase(Project project) {
        this.project = project;
        var projectDir = project.getProjectDir().toPath();

        project.getPluginManager().apply("java");


        this.sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

        this.mainSourceSet = sourceSets.getByName(MAIN_SOURCE_SET_NAME);

        writeString(
            createParentDirectories(projectDir.resolve("src/main/java/MainJavaClass.java")),
            "class MainJavaClass {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/main/kotlin/MainKotlinClass.kt")),
            "class MainKotlinClass {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/main/groovy/MainGroovyClass.groovy")),
            "class MainGroovyClass {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/main/resources/main-resource.txt")),
            "main resource"
        );


        this.testSourceSet = sourceSets.getByName(TEST_SOURCE_SET_NAME);

        writeString(
            createParentDirectories(projectDir.resolve("src/test/java/MainJavaClassTest.java")),
            "class javaMainJavaClassTest {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/test/kotlin/MainKotlinClassTest.kt")),
            "class MainKotlinClassTest {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/test/groovy/MainGroovyClassTest.groovy")),
            "class MainGroovyClassTest {}"
        );

        writeString(
            createParentDirectories(projectDir.resolve("src/test/resources/main-resource.txt")),
            "test resource"
        );
    }

}
