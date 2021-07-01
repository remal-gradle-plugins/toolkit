package name.remal.gradleplugins.toolkit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.junit.jupiter.api.Test;

class SourceSetUtilsTest {

    private final Project project;
    private final SourceSet mainSourceSet;
    private final SourceSet testSourceSet;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @SneakyThrows
    public SourceSetUtilsTest(Project project) {
        project.getPluginManager().apply("java");
        this.project = project;

        val sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

        this.mainSourceSet = sourceSets.getByName(MAIN_SOURCE_SET_NAME);
        val mainJavaDir = mainSourceSet.getJava().getSourceDirectories().getFiles().stream().findAny().get();
        createDirectories(mainJavaDir.toPath());
        write(mainJavaDir.toPath().resolve("MainJavaClass.java"), "class MainJavaClass {}".getBytes(UTF_8));
        val mainResourcesDir = mainSourceSet.getResources().getSourceDirectories().getFiles().stream().findAny().get();
        createDirectories(mainResourcesDir.toPath());
        write(mainResourcesDir.toPath().resolve("main-resource.txt"), "main resource".getBytes(UTF_8));

        this.testSourceSet = sourceSets.getByName(TEST_SOURCE_SET_NAME);
        val testJavaDir = testSourceSet.getJava().getSourceDirectories().getFiles().stream().findAny().get();
        createDirectories(testJavaDir.toPath());
        write(testJavaDir.toPath().resolve("TestJavaClass.java"), "class TestJavaClass {}".getBytes(UTF_8));
        val testResourcesDir = testSourceSet.getResources().getSourceDirectories().getFiles().stream().findAny().get();
        createDirectories(testResourcesDir.toPath());
        write(testResourcesDir.toPath().resolve("main-resource.txt"), "test resource".getBytes(UTF_8));
    }


    @Test
    void isProcessedBy_SourceTask() {
        val mainJavaCompile = project.getTasks().withType(JavaCompile.class)
            .getByName(mainSourceSet.getCompileJavaTaskName());
        assertTrue(SourceSetUtils.isProcessedBy(mainSourceSet, mainJavaCompile));
        assertFalse(SourceSetUtils.isProcessedBy(testSourceSet, mainJavaCompile));

        val testJavaCompile = project.getTasks().withType(JavaCompile.class)
            .getByName(testSourceSet.getCompileJavaTaskName());
        assertFalse(SourceSetUtils.isProcessedBy(mainSourceSet, testJavaCompile));
        assertTrue(SourceSetUtils.isProcessedBy(testSourceSet, testJavaCompile));
    }

    @Test
    void isProcessedBy_AbstractCopyTask() {
        val mainProcessResources = project.getTasks().withType(AbstractCopyTask.class)
            .getByName(mainSourceSet.getProcessResourcesTaskName());
        assertTrue(SourceSetUtils.isProcessedBy(mainSourceSet, mainProcessResources));
        assertFalse(SourceSetUtils.isProcessedBy(testSourceSet, mainProcessResources));

        val testProcessResources = project.getTasks().withType(AbstractCopyTask.class)
            .getByName(testSourceSet.getProcessResourcesTaskName());
        assertFalse(SourceSetUtils.isProcessedBy(mainSourceSet, testProcessResources));
        assertTrue(SourceSetUtils.isProcessedBy(testSourceSet, testProcessResources));
    }

    @Test
    void isCompiledBy() {
        val mainJavaCompile = project.getTasks().withType(JavaCompile.class)
            .getByName(mainSourceSet.getCompileJavaTaskName());
        assertTrue(SourceSetUtils.isCompiledBy(mainSourceSet, mainJavaCompile));
        assertFalse(SourceSetUtils.isCompiledBy(testSourceSet, mainJavaCompile));

        val testJavaCompile = project.getTasks().withType(JavaCompile.class)
            .getByName(testSourceSet.getCompileJavaTaskName());
        assertFalse(SourceSetUtils.isCompiledBy(mainSourceSet, testJavaCompile));
        assertTrue(SourceSetUtils.isCompiledBy(testSourceSet, testJavaCompile));
    }

}
