package name.remal.gradleplugins.testkit.functional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import name.remal.gradleplugins.testkit.AbstractJupiterTestEngineTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

class FunctionalTestExtensionTest extends AbstractJupiterTestEngineTests {

    @ExtendWith(FunctionalTestExtension.class)
    @ExtendWith(DisabledIfNotExecutedFromTestKit.class)
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790"})
    static class SimpleExample {

        @Test
        void simple_scenario(
            GradleProject project1,
            GradleProject project2
        ) {
            assertNotEquals(project1, project2);
            assertNotEquals(project1.getProjectDir(), project2.getProjectDir());
            assertTrue(project1.getProjectDir().isDirectory());
            assertTrue(project2.getProjectDir().isDirectory());

            project1.withoutPluginClasspath()
                .assertBuildSuccessfully();
        }

    }

    @Test
    void simple_scenario() {
        val tests = executeTestsForClass(SimpleExample.class).testEvents();
        tests.assertStatistics(stats -> stats.succeeded(1));
    }

}
