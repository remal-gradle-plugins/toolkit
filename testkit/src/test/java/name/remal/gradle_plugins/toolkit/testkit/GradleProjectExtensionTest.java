package name.remal.gradle_plugins.toolkit.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.SneakyThrowUtils;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;

@SuppressWarnings({"java:S5810", "java:S2699", "java:S5790", "NewClassNamingConvention", "JUnitMalformedDeclaration"})
class GradleProjectExtensionTest extends AbstractJupiterTestEngineTests {

    @RequiredArgsConstructor
    @ExtendWith(GradleProjectExtension.class)
    @ExampleTests
    static class SimpleConstructorExample {

        private final Project project;

        @Test
        void simple_scenario() {
            assertTrue(project.getProjectDir().isDirectory());
        }

    }

    @Test
    void simple_constructor_scenario() {
        val tests = executeTestsForClass(SimpleConstructorExample.class).testEvents();
        tests.failed().stream().forEach(event -> event.getPayload(TestExecutionResult.class)
            .flatMap(TestExecutionResult::getThrowable)
            .ifPresent(exception -> { throw SneakyThrowUtils.sneakyThrow(exception); })
        );
        tests.assertStatistics(stats -> stats.succeeded(1));
    }


    @ExtendWith(GradleProjectExtension.class)
    @ExampleTests
    static class SimpleMethodExample {

        @Test
        void simple_scenario(
            Project project1,
            Project project2
        ) {
            assertNotEquals(project1, project2);
            assertNotEquals(project1.getProjectDir(), project2.getProjectDir());
            assertTrue(project1.getProjectDir().isDirectory());
            assertTrue(project2.getProjectDir().isDirectory());
        }

    }

    @Test
    void simple_method_scenario() {
        val tests = executeTestsForClass(SimpleMethodExample.class).testEvents();
        tests.failed().stream().forEach(event -> event.getPayload(TestExecutionResult.class)
            .flatMap(TestExecutionResult::getThrowable)
            .ifPresent(exception -> { throw SneakyThrowUtils.sneakyThrow(exception); })
        );
        tests.assertStatistics(stats -> stats.succeeded(1));
    }


    @ExtendWith(GradleProjectExtension.class)
    @ExampleTests
    @RequiredArgsConstructor
    static class InjectedOnceExample {

        private final Project project;

        private void injected_once() {
            // Check that property is not set
            assertNull(project.findProperty("injected_once"));
            // Set the property, so if the other test uses the same project instance, that task will fail
            project.getExtensions().getExtraProperties().set("injected_once", true);
        }

        @Test
        void injected_once1() {
            injected_once();
        }

        @Test
        void injected_once2() {
            injected_once();
        }

    }

    @Test
    void injected_once() {
        val tests = executeTestsForClass(InjectedOnceExample.class).testEvents();
        tests.failed().stream().forEach(event -> event.getPayload(TestExecutionResult.class)
            .flatMap(TestExecutionResult::getThrowable)
            .ifPresent(exception -> { throw SneakyThrowUtils.sneakyThrow(exception); })
        );
        tests.assertStatistics(stats -> stats.succeeded(2));
    }


    @RequiredArgsConstructor
    @ExtendWith(GradleProjectExtension.class)
    @ExampleTests
    static class FullConstructorExample {

        @ChildProjectOf("parentProject")
        private final Project childProject;
        @ChildProjectOf("rootProject")
        private final Project parentProject;
        private final Project rootProject;

        @Test
        void full_scenario() {
            assertNotNull(childProject.getParent());
            assertEquals(parentProject.getProjectDir(), childProject.getParent().getProjectDir());
            assertNotNull(parentProject.getParent());
            assertEquals(rootProject.getProjectDir(), parentProject.getParent().getProjectDir());
            assertNull(rootProject.getParent());
        }

    }

    @Test
    void full_constructor_scenario() {
        val tests = executeTestsForClass(FullConstructorExample.class).testEvents();
        tests.failed().stream().forEach(event -> event.getPayload(TestExecutionResult.class)
            .flatMap(TestExecutionResult::getThrowable)
            .ifPresent(exception -> { throw SneakyThrowUtils.sneakyThrow(exception); })
        );
        tests.assertStatistics(stats -> stats.succeeded(1));
    }


    @ExtendWith(GradleProjectExtension.class)
    @ExampleTests
    static class FullMethodExample {

        @Test
        void full_scenario(
            @ChildProjectOf("parentProject") Project childProject,
            @ChildProjectOf("rootProject") Project parentProject,
            Project rootProject
        ) {
            assertNotNull(childProject.getParent());
            assertEquals(parentProject.getProjectDir(), childProject.getParent().getProjectDir());
            assertNotNull(parentProject.getParent());
            assertEquals(rootProject.getProjectDir(), parentProject.getParent().getProjectDir());
            assertNull(rootProject.getParent());
        }

    }

    @Test
    void full_method_scenario() {
        val tests = executeTestsForClass(FullMethodExample.class).testEvents();
        tests.failed().stream().forEach(event ->
            event.getPayload(TestExecutionResult.class)
                .flatMap(TestExecutionResult::getThrowable)
                .ifPresent(exception -> { throw SneakyThrowUtils.sneakyThrow(exception); })
        );
        tests.assertStatistics(stats -> stats.succeeded(1));
    }


    @ExtendWith(GradleProjectExtension.class)
    @ExampleTests
    static class ApplyPluginExample {

        @Test
        void apply_plugin(@ApplyPlugin(value = "java", type = JavaLibraryPlugin.class) Project project) {
            assertTrue(project.getPlugins().hasPlugin("java"));
            assertTrue(project.getPlugins().hasPlugin(JavaLibraryPlugin.class));
        }

    }

    @Test
    void apply_plugin() {
        val tests = executeTestsForClass(ApplyPluginExample.class).testEvents();
        tests.failed().stream().forEach(event -> event.getPayload(TestExecutionResult.class)
            .flatMap(TestExecutionResult::getThrowable)
            .ifPresent(exception -> { throw SneakyThrowUtils.sneakyThrow(exception); })
        );
        tests.assertStatistics(stats -> stats.succeeded(1));
    }


    @ExtendWith(GradleProjectExtension.class)
    @ExampleTests
    @SuppressWarnings("unused")
    static class ReferencesToItselfExample {

        @Test
        void references_to_itself(
            @ChildProjectOf("childProject") Project childProject,
            Project parentProject
        ) {
            // should fail
        }

    }

    @Test
    void references_to_itself() {
        val tests = executeTestsForClass(ReferencesToItselfExample.class).testEvents();
        tests.assertStatistics(stats -> stats.failed(1));
    }


    @ExtendWith(GradleProjectExtension.class)
    @ExampleTests
    @SuppressWarnings("unused")
    static class InvalidReferenceExample {

        @Test
        void invalid_reference(
            @ChildProjectOf("parent") Project childProject,
            Project parentProject
        ) {
            // should fail
        }

    }

    @Test
    void invalid_reference() {
        val tests = executeTestsForClass(InvalidReferenceExample.class).testEvents();
        tests.assertStatistics(stats -> stats.failed(1));
    }

}
