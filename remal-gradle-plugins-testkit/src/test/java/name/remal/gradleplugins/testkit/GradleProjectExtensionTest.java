package name.remal.gradleplugins.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

class GradleProjectExtensionTest extends AbstractJupiterTestEngineTests {

    @ExtendWith(GradleProjectExtension.class)
    @ExtendWith(DisabledIfNotExecutedFromTestKit.class)
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
    static class SimpleExample {

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
    void simple_scenario() {
        val tests = executeTestsForClass(SimpleExample.class).testEvents();
        tests.assertStatistics(stats -> stats.succeeded(1));
    }


    @ExtendWith(GradleProjectExtension.class)
    @ExtendWith(DisabledIfNotExecutedFromTestKit.class)
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
    static class FullExample {

        @Test
        void full_scenario(
            @ChildProjectOf("parentProject") Project childProject,
            @ChildProjectOf("rootProject") Project parentProject,
            Project rootProject
        ) {
            assertEquals(parentProject, childProject.getParent());
            assertEquals(rootProject, parentProject.getParent());
            assertNull(rootProject.getParent());
        }

    }

    @Test
    void full_scenario() {
        val tests = executeTestsForClass(FullExample.class).testEvents();
        tests.assertStatistics(stats -> stats.succeeded(1));
    }


    @ExtendWith(GradleProjectExtension.class)
    @ExtendWith(DisabledIfNotExecutedFromTestKit.class)
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
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
    @ExtendWith(DisabledIfNotExecutedFromTestKit.class)
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
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
