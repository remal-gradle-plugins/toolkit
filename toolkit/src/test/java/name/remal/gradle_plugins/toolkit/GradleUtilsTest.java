package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.invocation.Gradle;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class GradleUtilsTest {

    private final Gradle gradle;

    @Test
    void onGradleBuildFinished_doe_not_throw_exception() {
        Action<? super Gradle> action = __ -> { };
        assertDoesNotThrow(() -> GradleUtils.onGradleBuildFinished(gradle, action));
    }

}
