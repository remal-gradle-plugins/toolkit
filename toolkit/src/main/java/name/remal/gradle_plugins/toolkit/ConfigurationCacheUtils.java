package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.UNKNOWN;
import static name.remal.gradle_plugins.toolkit.PluginUtils.findPluginIdFor;
import static name.remal.gradle_plugins.toolkit.PluginUtils.getPluginIdWithoutCorePrefix;
import static name.remal.gradle_plugins.toolkit.PluginUtils.isCorePluginId;

import lombok.NoArgsConstructor;
import org.gradle.api.Plugin;
import org.gradle.util.GradleVersion;

@NoArgsConstructor(access = PRIVATE)
public abstract class ConfigurationCacheUtils {

    public static GradleCompatibilityMode getCorePluginConfigurationCacheSupport(String corePluginId) {
        corePluginId = getPluginIdWithoutCorePrefix(corePluginId);
        if (!isCorePluginId(corePluginId)) {
            return UNKNOWN;
        }

        return CorePluginConfigurationCacheSupport.get(GradleVersion.current(), corePluginId);
    }

    public static GradleCompatibilityMode getCorePluginConfigurationCacheSupport(
        Class<? extends Plugin<?>> pluginClass
    ) {
        var pluginId = findPluginIdFor(pluginClass);
        if (pluginId != null) {
            return getCorePluginConfigurationCacheSupport(pluginId);
        }

        return UNKNOWN;
    }

}
