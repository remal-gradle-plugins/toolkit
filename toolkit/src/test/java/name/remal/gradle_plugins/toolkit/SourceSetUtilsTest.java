package name.remal.gradle_plugins.toolkit;

import static java.util.stream.Collectors.toUnmodifiableList;
import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.SourceSetUtils.whenTestSourceSetRegistered;
import static name.remal.gradle_plugins.toolkit.testkit.ProjectValidations.executeAfterEvaluateActions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableGradleVersion;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.testing.base.TestingExtension;
import org.junit.jupiter.api.Test;

class SourceSetUtilsTest extends SourceSetUtilsTestBase {

    SourceSetUtilsTest(Project project) {
        super(project);
    }


    @Test
    void getSourceSetConfigurationNames() {
        var mainConfNames = SourceSetUtils.getSourceSetConfigurationNames(mainSourceSet);
        assertThat(mainConfNames).contains(
            "implementation",
            "compileOnly"
        );

        var testConfNames = SourceSetUtils.getSourceSetConfigurationNames(testSourceSet);
        assertThat(testConfNames).contains(
            "testImplementation",
            "testCompileOnly"
        );
    }


    @Test
    void isProcessedBy_SourceTask() {
        var mainJavaCompile = tasks.withType(JavaCompile.class)
            .getByName(mainSourceSet.getCompileJavaTaskName());
        assertTrue(SourceSetUtils.isProcessedBy(mainSourceSet, mainJavaCompile));
        assertFalse(SourceSetUtils.isProcessedBy(testSourceSet, mainJavaCompile));

        var testJavaCompile = tasks.withType(JavaCompile.class)
            .getByName(testSourceSet.getCompileJavaTaskName());
        assertFalse(SourceSetUtils.isProcessedBy(mainSourceSet, testJavaCompile));
        assertTrue(SourceSetUtils.isProcessedBy(testSourceSet, testJavaCompile));
    }

    @Test
    void isProcessedBy_AbstractCopyTask() {
        var mainProcessResources = tasks.withType(AbstractCopyTask.class)
            .getByName(mainSourceSet.getProcessResourcesTaskName());
        assertTrue(SourceSetUtils.isProcessedBy(mainSourceSet, mainProcessResources));
        assertFalse(SourceSetUtils.isProcessedBy(testSourceSet, mainProcessResources));

        var testProcessResources = tasks.withType(AbstractCopyTask.class)
            .getByName(testSourceSet.getProcessResourcesTaskName());
        assertFalse(SourceSetUtils.isProcessedBy(mainSourceSet, testProcessResources));
        assertTrue(SourceSetUtils.isProcessedBy(testSourceSet, testProcessResources));
    }

    @Test
    @TagKotlinPlugin
    void isProcessedBy_kotlin() {
        project.getPluginManager().apply("org.jetbrains.kotlin.jvm");
        executeAfterEvaluateActions(project); // old Kotlin plugins configure a lot of things on afterEvaluate()

        var mainKotlinCompile = tasks
            .getByName(mainSourceSet.getCompileTaskName("kotlin"));
        assertTrue(SourceSetUtils.isProcessedBy(mainSourceSet, mainKotlinCompile));
        assertFalse(SourceSetUtils.isProcessedBy(testSourceSet, mainKotlinCompile));

        var testKotlinCompile = tasks
            .getByName(testSourceSet.getCompileTaskName("kotlin"));
        assertFalse(SourceSetUtils.isProcessedBy(mainSourceSet, testKotlinCompile));
        assertTrue(SourceSetUtils.isProcessedBy(testSourceSet, testKotlinCompile));
    }


    @Test
    void isCompiledBy() {
        var mainJavaCompile = tasks.withType(JavaCompile.class)
            .getByName(mainSourceSet.getCompileJavaTaskName());
        assertTrue(SourceSetUtils.isCompiledBy(mainSourceSet, mainJavaCompile));
        assertFalse(SourceSetUtils.isCompiledBy(testSourceSet, mainJavaCompile));

        var testJavaCompile = tasks.withType(JavaCompile.class)
            .getByName(testSourceSet.getCompileJavaTaskName());
        assertFalse(SourceSetUtils.isCompiledBy(mainSourceSet, testJavaCompile));
        assertTrue(SourceSetUtils.isCompiledBy(testSourceSet, testJavaCompile));
    }

    @Test
    @TagKotlinPlugin
    void isCompiledBy_kotlin() {
        project.getPluginManager().apply("org.jetbrains.kotlin.jvm");
        executeAfterEvaluateActions(project); // old Kotlin plugins configure a lot of things on afterEvaluate()

        var mainKotlinCompile = tasks
            .getByName(mainSourceSet.getCompileTaskName("kotlin"));
        assertTrue(SourceSetUtils.isCompiledBy(mainSourceSet, mainKotlinCompile));
        assertFalse(SourceSetUtils.isCompiledBy(testSourceSet, mainKotlinCompile));

        var testKotlinCompile = tasks
            .getByName(testSourceSet.getCompileTaskName("kotlin"));
        assertFalse(SourceSetUtils.isCompiledBy(mainSourceSet, testKotlinCompile));
        assertTrue(SourceSetUtils.isCompiledBy(testSourceSet, testKotlinCompile));
    }


    @Test
    void getAllSourceDirectorySets() {
        project.getPluginManager().apply("groovy");

        var allSourceDirectorySetNames = SourceSetUtils.getAllSourceDirectorySets(mainSourceSet).stream()
            .map(SourceDirectorySet::getName)
            .collect(toUnmodifiableList());
        assertThat(allSourceDirectorySetNames)
            .contains(
                "allsource",
                "java",
                "alljava",
                "resources",
                "groovy"
            );
    }

    @Test
    @TagKotlinPlugin
    void getAllSourceDirectorySets_kotlin() {
        project.getPluginManager().apply("org.jetbrains.kotlin.jvm");
        executeAfterEvaluateActions(project); // old Kotlin plugins configure a lot of things on afterEvaluate()

        var allSourceDirectorySetNames = SourceSetUtils.getAllSourceDirectorySets(mainSourceSet).stream()
            .map(SourceDirectorySet::getName)
            .collect(toUnmodifiableList());
        assertThat(allSourceDirectorySetNames)
            .contains(
                "allsource",
                "java",
                "alljava",
                "resources"
            );
        assertThat(allSourceDirectorySetNames)
            .containsAnyOf(
                "main kotlin",
                "main Kotlin source"
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
    @MinTestableGradleVersion("7.3")
    void whenTestSourceSetRegistered_jvm_test_suite() {
        Collection<SourceSet> testSourceSets = new ArrayList<>();
        whenTestSourceSetRegistered(project, testSourceSets::add);

        project.getPluginManager().apply("jvm-test-suite");

        var testingExtension = getExtension(project, TestingExtension.class);
        testingExtension.getSuites().create("integration", JvmTestSuite.class);

        assertThat(testSourceSets)
            .extracting(SourceSet::getName)
            .containsExactlyInAnyOrder(
                "test",
                "integration"
            );
    }

    @Test
    @MinTestableGradleVersion("5.6")
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
