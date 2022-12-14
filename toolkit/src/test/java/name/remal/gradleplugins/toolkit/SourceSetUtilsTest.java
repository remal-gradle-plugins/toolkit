package name.remal.gradleplugins.toolkit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.write;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradleplugins.toolkit.SourceSetUtils.whenTestSourceSetRegistered;
import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradleplugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testing.base.TestingExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SourceSetUtilsTest {

    private final Project project;
    private final SourceSetContainer sourceSets;
    private final SourceSet mainSourceSet;
    private final SourceSet testSourceSet;
    private final File tempFile;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @SneakyThrows
    public SourceSetUtilsTest(Project project) {
        project.getPluginManager().apply("java");
        this.project = project;

        this.sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

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

        this.tempFile = createTempFile("file-", ".temp").toFile();
    }

    @AfterEach
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void afterEach() {
        tempFile.delete();
    }


    @Test
    void getSourceSetConfigurationNames() {
        val mainConfNames = SourceSetUtils.getSourceSetConfigurationNames(mainSourceSet);
        assertThat(mainConfNames).contains(
            "implementation",
            "compileOnly"
        );

        val testConfNames = SourceSetUtils.getSourceSetConfigurationNames(testSourceSet);
        assertThat(testConfNames).contains(
            "testImplementation",
            "testCompileOnly"
        );
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


    @Test
    void getAllSourceDirectorySets() {
        project.getPluginManager().apply("groovy");

        assertThat(SourceSetUtils.getAllSourceDirectorySets(mainSourceSet))
            .extracting(SourceDirectorySet::getName)
            .containsExactlyInAnyOrder(
                "allsource",
                "java",
                "alljava",
                "resources",
                "groovy",
                "allgroovy"
            );
    }


    @Test
    void whenTestSourceSetRegistered_default() {
        Collection<SourceSet> testSourceSets = new ArrayList<>();
        whenTestSourceSetRegistered(project, testSourceSets::add);
        assertThat(testSourceSets)
            .extracting(SourceSet::getName)
            .containsExactlyInAnyOrder(
                "test"
            );
    }

    @Test
    @MinSupportedGradleVersion("7.3")
    @SuppressWarnings("UnstableApiUsage")
    void whenTestSourceSetRegistered_jvm_test_suite() {
        Collection<SourceSet> testSourceSets = new ArrayList<>();
        whenTestSourceSetRegistered(project, testSourceSets::add);

        project.getPluginManager().apply("jvm-test-suite");

        val testingExtension = getExtension(project, TestingExtension.class);
        testingExtension.getSuites().create("integration", JvmTestSuite.class);

        assertThat(testSourceSets)
            .extracting(SourceSet::getName)
            .containsExactlyInAnyOrder(
                "test",
                "integration"
            );
    }

    @Test
    @MinSupportedGradleVersion("5.6")
    void whenTestSourceSetRegistered_test_fixtures() {
        Collection<SourceSet> testSourceSets = new ArrayList<>();
        whenTestSourceSetRegistered(project, testSourceSets::add);

        project.getPluginManager().apply("java-test-fixtures");

        assertThat(testSourceSets)
            .extracting(SourceSet::getName)
            .containsExactlyInAnyOrder(
                "test",
                "testFixtures"
            );
    }

    @Test
    void whenTestSourceSetRegistered_by_suffix() {
        Collection<SourceSet> testSourceSets = new ArrayList<>();
        whenTestSourceSetRegistered(project, testSourceSets::add);

        sourceSets.create("integrationTest");
        sourceSets.create("integrationTests");

        sourceSets.create("functional-test");
        sourceSets.create("functional-tests");

        sourceSets.create("functional_test");
        sourceSets.create("functional_tests");

        assertThat(testSourceSets)
            .extracting(SourceSet::getName)
            .containsExactlyInAnyOrder(
                "test",
                "integrationTest",
                "integrationTests",
                "functional-test",
                "functional-tests",
                "functional_test",
                "functional_tests"
            );
    }

}
