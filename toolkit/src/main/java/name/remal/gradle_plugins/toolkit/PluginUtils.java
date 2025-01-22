package name.remal.gradle_plugins.toolkit;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.reflection.WhoCalledUtils.getCallingClass;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.github.classgraph.ClassGraph;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.Plugin;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class PluginUtils {

    private static final String CORE_PLUGIN_ID_PREFIX = "org.gradle.";

    private static final LoadingCache<ClassLoader, Map<String, String>> PLUGIN_CLASS_NAMES = CacheBuilder.newBuilder()
        .weakKeys()
        .build(CacheLoader.from(PluginUtils::getAllPluginClassNamesFor));

    @Contract(pure = true)
    @SneakyThrows
    public static Map<String, String> getAllPluginClassNames(@Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getSystemClassLoader();
        }
        return PLUGIN_CLASS_NAMES.getUnchecked(classLoader);
    }

    @Contract(pure = true)
    public static Map<String, String> getAllPluginClassNames() {
        var callingClass = getCallingClass(2);
        return getAllPluginClassNames(callingClass.getClassLoader());
    }

    @Contract(pure = true)
    @SneakyThrows
    private static Map<String, String> getAllPluginClassNamesFor(ClassLoader classLoader) {
        Map<String, String> result = new TreeMap<>();

        var resourceDir = "META-INF/gradle-plugins";
        try (
            var scanResult = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .acceptPathsNonRecursive(resourceDir)
                .scan()
        ) {
            var pathPrefix = resourceDir + '/';
            var pathSuffix = ".properties";
            for (var resource : scanResult.getAllResources()) {
                var path = resource.getPath();
                String pluginId = path.substring(pathPrefix.length(), path.length() - pathSuffix.length());
                pluginId = getPluginIdWithoutCorePrefix(pluginId);
                if (isEmpty(pluginId)) {
                    continue;
                }

                var properties = new Properties();
                try (var reader = new InputStreamReader(resource.open(), UTF_8)) {
                    properties.load(reader);
                }
                var implementationClassName = properties.getProperty("implementation-class");
                if (isEmpty(implementationClassName)) {
                    continue;
                }

                result.putIfAbsent(pluginId, implementationClassName);
            }
        }

        return ImmutableMap.copyOf(result);
    }


    @Contract(pure = true)
    @Nullable
    public static String findPluginIdFor(Class<? extends Plugin<?>> pluginType) {
        var pluginClassNames = getAllPluginClassNames(pluginType.getClassLoader());
        for (var entry : pluginClassNames.entrySet()) {
            if (pluginType.getName().equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }


    @Contract(pure = true)
    public static String getPluginIdWithoutCorePrefix(String pluginId) {
        if (pluginId.startsWith(CORE_PLUGIN_ID_PREFIX)) {
            return pluginId.substring(CORE_PLUGIN_ID_PREFIX.length());
        } else {
            return pluginId;
        }
    }

    @Contract(pure = true)
    public static boolean isCorePluginId(String pluginId) {
        pluginId = getPluginIdWithoutCorePrefix(pluginId);
        return PluginUtils.class.getResource(format(
            "/META-INF/gradle-plugins/%s%s.properties",
            CORE_PLUGIN_ID_PREFIX,
            pluginId
        )) != null;
    }

}
