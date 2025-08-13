package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.PARTIALLY_SUPPORTED;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.SUPPORTED;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.UNSUPPORTED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import name.remal.gradle_plugins.toolkit.testkit.MaxTestableGradleVersion;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableGradleVersion;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.Test;

@MinTestableGradleVersion("6.6")
class CorePluginConfigurationCacheSupportTest {

    @Test
    void base() {
        assertEquals(SUPPORTED, getSupportOf("base"));
    }

    @Test
    void java() {
        assertEquals(SUPPORTED, getSupportOf("java"));
    }

    @Test
    void java_library() {
        assertEquals(SUPPORTED, getSupportOf("java-library"));
    }

    @Test
    void java_platform() {
        assertEquals(SUPPORTED, getSupportOf("java-platform"));
    }

    @Test
    @MinTestableGradleVersion("7.2")
    void groovy() {
        assertEquals(SUPPORTED, getSupportOf("groovy"));
    }

    @Test
    @MaxTestableGradleVersion("7.1.9999")
    void groovy_lte_7_1() {
        assertEquals(PARTIALLY_SUPPORTED, getSupportOf("groovy"));
    }

    @Test
    @MinTestableGradleVersion("7.2")
    void scala() {
        assertEquals(SUPPORTED, getSupportOf("scala"));
    }

    @Test
    @MaxTestableGradleVersion("7.1.9999")
    void scala_lte_7_1() {
        assertEquals(UNSUPPORTED, getSupportOf("scala"));
    }

    @Test
    void application() {
        if (GradleVersion.current().equals(GradleVersion.version("8.0.2"))) {
            assertEquals(PARTIALLY_SUPPORTED, getSupportOf("application"));
            return;
        }

        assertEquals(SUPPORTED, getSupportOf("application"));
    }

    @Test
    @MinTestableGradleVersion("6.8")
    void checkstyle() {
        assertEquals(SUPPORTED, getSupportOf("checkstyle"));
    }

    @Test
    @MaxTestableGradleVersion("6.7.9999")
    void checkstyle_lte_6_7() {
        assertEquals(UNSUPPORTED, getSupportOf("checkstyle"));
    }

    @Test
    @MinTestableGradleVersion("6.8")
    void pmd() {
        assertEquals(SUPPORTED, getSupportOf("pmd"));
    }

    @Test
    @MaxTestableGradleVersion("6.7.9999")
    void pmd_lte_6_7() {
        assertEquals(UNSUPPORTED, getSupportOf("pmd"));
    }

    @Test
    @MinTestableGradleVersion("6.8")
    void jacoco() {
        assertEquals(SUPPORTED, getSupportOf("jacoco"));
    }

    @Test
    @MaxTestableGradleVersion("6.7.9999")
    void jacoco_lte_6_7() {
        assertEquals(UNSUPPORTED, getSupportOf("jacoco"));
    }

    @Test
    @MinTestableGradleVersion("8.1")
    void signing() {
        assertEquals(SUPPORTED, getSupportOf("signing"));
    }

    @Test
    @MaxTestableGradleVersion("8.0.9999")
    void signing_lte_8_0() {
        assertEquals(UNSUPPORTED, getSupportOf("signing"));
    }


    private static GradleCompatibilityMode getSupportOf(String pluginId) {
        return CorePluginConfigurationCacheSupport.get(GradleVersion.current(), pluginId);
    }

}
