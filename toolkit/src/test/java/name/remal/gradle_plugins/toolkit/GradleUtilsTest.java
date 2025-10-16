package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.GradleUtils.afterProjectWithLifecycleSupport;
import static name.remal.gradle_plugins.toolkit.GradleUtils.beforeProjectWithLifecycleSupport;
import static name.remal.gradle_plugins.toolkit.GradleUtils.onGradleBuildFinished;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import lombok.RequiredArgsConstructor;
import org.gradle.api.invocation.Gradle;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class GradleUtilsTest {

    final Gradle gradle;

    @Test
    void onGradleBuildFinished_does_not_throw_exception() {
        assertDoesNotThrow(() -> onGradleBuildFinished(gradle, __ -> { }));
    }

    @Test
    void beforeProjectWithLifecycleSupport_does_not_throw_exception() {
        assertDoesNotThrow(() -> beforeProjectWithLifecycleSupport(gradle, __ -> { }));
    }

    @Test
    void afterProjectWithLifecycleSupport_does_not_throw_exception() {
        assertDoesNotThrow(() -> afterProjectWithLifecycleSupport(gradle, __ -> { }));
    }

}
