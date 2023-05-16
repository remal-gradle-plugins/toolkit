package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.function.Supplier;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.AbstractJupiterTestEngineTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.Events;

class FunctionalTestExtensionTest extends AbstractJupiterTestEngineTests {

    @ExtendWith(FunctionalTestExtension.class)
    @ExampleTests
    @SuppressWarnings({"java:S5810", "java:S2699", "java:S5790", "NewClassNamingConvention"})
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
        ImmutableMap.<String, Supplier<Events>>of(
            "SKIPPED", tests::skipped,
            "ABORTED", tests::aborted,
            "FAILED", tests::failed
        ).forEach((type, eventsSupplier) -> {
            val events = eventsSupplier.get();
            events.stream().forEach(event -> {
                throw new AssertionError(format(
                    "%s event: %s",
                    type,
                    event
                ));
            });
        });

        tests.assertStatistics(stats -> stats.succeeded(1));
    }

}
