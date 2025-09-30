package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.SUPPORTED;
import static name.remal.gradle_plugins.toolkit.GradleCompatibilityMode.UNKNOWN;
import static name.remal.gradle_plugins.toolkit.PluginUtils.findPluginIdFor;
import static name.remal.gradle_plugins.toolkit.PluginUtils.getPluginIdWithoutCorePrefix;
import static name.remal.gradle_plugins.toolkit.PluginUtils.isCorePluginId;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.NoArgsConstructor;
import org.gradle.api.Plugin;
import org.gradle.util.GradleVersion;

@NoArgsConstructor(access = PRIVATE)
public abstract class ConfigurationCacheUtils {

    private static final Map<String, GradleCompatibilityMode> KNOWN_PLUGINS =
        ImmutableMap.<String, GradleCompatibilityMode>builder()
            .put("org.gradle.toolchains.foojay-resolver", SUPPORTED)
            .put("org.gradle.toolchains.foojay-resolver-convention", SUPPORTED)
            .build();

    public static GradleCompatibilityMode getPluginConfigurationCacheSupport(String pluginId) {
        var compatibility = KNOWN_PLUGINS.get(pluginId);

        if (compatibility == null) {
            compatibility = getCorePluginConfigurationCacheSupport(pluginId);
        }

        return compatibility;
    }

    public static GradleCompatibilityMode getPluginConfigurationCacheSupport(
        Class<? extends Plugin<?>> pluginClass
    ) {
        var pluginId = findPluginIdFor(pluginClass);
        if (pluginId != null) {
            return getPluginConfigurationCacheSupport(pluginId);
        }

        return UNKNOWN;
    }


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
