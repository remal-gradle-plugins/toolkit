package name.remal.gradle_plugins.toolkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.gradle.api.Plugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.junit.jupiter.api.Test;

class PluginUtilsTest {

    @Test
    void getAllPluginClassNames() {
        assertThat(PluginUtils.getAllPluginClassNames())
            .contains(
                entry("java", "org.gradle.api.plugins.JavaPlugin"),
                entry("java-library", "org.gradle.api.plugins.JavaLibraryPlugin"),
                entry("checkstyle", "org.gradle.api.plugins.quality.CheckstylePlugin")
            );
    }

    @Test
    void findPluginIdFor() {
        class UnknownPlugin implements Plugin<Object> {
            @Override
            public void apply(Object target) {
                // do nothing
            }
        }

        assertThat(PluginUtils.findPluginIdFor(JavaPlugin.class))
            .isEqualTo("java");
        assertThat(PluginUtils.findPluginIdFor(JavaLibraryPlugin.class))
            .isEqualTo("java-library");
        assertThat(PluginUtils.findPluginIdFor(CheckstylePlugin.class))
            .isEqualTo("checkstyle");
        assertThat(PluginUtils.findPluginIdFor(UnknownPlugin.class))
            .isNull();
    }

    @Test
    void getPluginIdWithoutCorePrefix() {
        assertThat(PluginUtils.getPluginIdWithoutCorePrefix("org.gradle.java"))
            .isEqualTo("java");
        assertThat(PluginUtils.getPluginIdWithoutCorePrefix("java"))
            .isEqualTo("java");
        assertThat(PluginUtils.getPluginIdWithoutCorePrefix("asd.java"))
            .isEqualTo("asd.java");
    }

    @Test
    void isCorePluginId() {
        assertThat(PluginUtils.isCorePluginId("org.gradle.java"))
            .isTrue();
        assertThat(PluginUtils.isCorePluginId("java"))
            .isTrue();
    }

}
