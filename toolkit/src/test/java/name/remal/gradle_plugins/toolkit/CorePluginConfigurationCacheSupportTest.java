package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ConfigurationCachePluginSupport.PARTIALLY_SUPPORTED;
import static name.remal.gradle_plugins.toolkit.ConfigurationCachePluginSupport.SUPPORTED;
import static name.remal.gradle_plugins.toolkit.ConfigurationCachePluginSupport.UNSUPPORTED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import name.remal.gradle_plugins.toolkit.testkit.MaxSupportedGradleVersion;
import name.remal.gradle_plugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.Test;

@MinSupportedGradleVersion("6.6")
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
    @MinSupportedGradleVersion("7.2")
    void groovy() {
        assertEquals(SUPPORTED, getSupportOf("groovy"));
    }

    @Test
    @MaxSupportedGradleVersion("7.1.9999")
    void groovy_lte_7_1() {
        assertEquals(PARTIALLY_SUPPORTED, getSupportOf("groovy"));
    }

    @Test
    @MinSupportedGradleVersion("7.2")
    void scala() {
        assertEquals(SUPPORTED, getSupportOf("scala"));
    }

    @Test
    @MaxSupportedGradleVersion("7.1.9999")
    void scala_lte_7_1() {
        assertEquals(UNSUPPORTED, getSupportOf("scala"));
    }

    @Test
    void application() {
        assertEquals(SUPPORTED, getSupportOf("application"));
    }

    @Test
    @MinSupportedGradleVersion("6.8")
    void checkstyle() {
        assertEquals(SUPPORTED, getSupportOf("checkstyle"));
    }

    @Test
    @MaxSupportedGradleVersion("6.7.9999")
    void checkstyle_lte_6_7() {
        assertEquals(UNSUPPORTED, getSupportOf("checkstyle"));
    }

    @Test
    @MinSupportedGradleVersion("6.8")
    void pmd() {
        assertEquals(SUPPORTED, getSupportOf("pmd"));
    }

    @Test
    @MaxSupportedGradleVersion("6.7.9999")
    void pmd_lte_6_7() {
        assertEquals(UNSUPPORTED, getSupportOf("pmd"));
    }

    @Test
    @MinSupportedGradleVersion("6.8")
    void jacoco() {
        assertEquals(SUPPORTED, getSupportOf("jacoco"));
    }

    @Test
    @MaxSupportedGradleVersion("6.7.9999")
    void jacoco_lte_6_7() {
        assertEquals(UNSUPPORTED, getSupportOf("jacoco"));
    }

    @Test
    @MinSupportedGradleVersion("8.1")
    void signing() {
        assertEquals(SUPPORTED, getSupportOf("signing"));
    }

    @Test
    @MaxSupportedGradleVersion("8.0.9999")
    void signing_lte_8_0() {
        assertEquals(UNSUPPORTED, getSupportOf("signing"));
    }


    private static ConfigurationCachePluginSupport getSupportOf(String pluginId) {
        return CorePluginConfigurationCacheSupport.get(GradleVersion.current(), pluginId);
    }

}
