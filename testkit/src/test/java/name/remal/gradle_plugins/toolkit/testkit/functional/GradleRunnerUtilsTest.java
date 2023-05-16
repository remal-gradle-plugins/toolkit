package name.remal.gradle_plugins.toolkit.testkit.functional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

class GradleRunnerUtilsTest {

    private final GradleRunner runner = GradleRunner.create();

    @Test
    void jvmArguments() {
        assertDoesNotThrow(() ->
            GradleRunnerUtils.withJvmArguments(runner, "-Dprop=123")
        );
    }

}
