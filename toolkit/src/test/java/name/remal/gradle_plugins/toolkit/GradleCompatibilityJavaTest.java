package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.SUPPORTED;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.UNKNOWN;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.UNSUPPORTED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.Test;

class GradleCompatibilityJavaTest {

    @Test
    void test() {
        assertEquals(UNKNOWN, getSupportOf("8.6", 22));

        assertEquals(UNSUPPORTED, getSupportOf("8.5", 22));
        assertEquals(SUPPORTED, getSupportOf("8.5", 21));
        assertEquals(SUPPORTED, getSupportOf("8.5", 20));

        assertEquals(UNSUPPORTED, getSupportOf("7.3", 18));
        assertEquals(SUPPORTED, getSupportOf("7.3", 17));
        assertEquals(SUPPORTED, getSupportOf("7.3", 16));

        assertEquals(UNSUPPORTED, getSupportOf("5.0", 12));
        assertEquals(SUPPORTED, getSupportOf("5.0", 11));
        assertEquals(SUPPORTED, getSupportOf("5.0", 10));

        assertEquals(UNSUPPORTED, getSupportOf("2.0", 9));
        assertEquals(SUPPORTED, getSupportOf("2.0", 8));
        assertEquals(SUPPORTED, getSupportOf("2.0", 7));

        assertEquals(UNKNOWN, getSupportOf("1.0", 8));
    }

    private static GradleCompatibilityMode getSupportOf(String gradleVersion, int javaVersion) {
        return GradleCompatibilityJava.get(GradleVersion.version(gradleVersion), javaVersion);
    }

}
