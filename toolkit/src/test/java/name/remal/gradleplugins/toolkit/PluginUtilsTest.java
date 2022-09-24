package name.remal.gradleplugins.toolkit;

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
                entry("org.gradle.java", "org.gradle.api.plugins.JavaPlugin"),
                entry("org.gradle.java-library", "org.gradle.api.plugins.JavaLibraryPlugin"),
                entry("org.gradle.checkstyle", "org.gradle.api.plugins.quality.CheckstylePlugin")
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
            .isEqualTo("org.gradle.java");
        assertThat(PluginUtils.findPluginIdFor(JavaLibraryPlugin.class))
            .isEqualTo("org.gradle.java-library");
        assertThat(PluginUtils.findPluginIdFor(CheckstylePlugin.class))
            .isEqualTo("org.gradle.checkstyle");
        assertThat(PluginUtils.findPluginIdFor(UnknownPlugin.class))
            .isNull();
    }

}
