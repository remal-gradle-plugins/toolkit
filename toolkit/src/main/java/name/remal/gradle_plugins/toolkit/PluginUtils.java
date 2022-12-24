package name.remal.gradle_plugins.toolkit;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Collections.synchronizedMap;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.reflection.WhoCalledUtils.getCallingClass;

import com.google.common.collect.ImmutableMap;
import io.github.classgraph.ClassGraph;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Plugin;

@NoArgsConstructor(access = PRIVATE)
public abstract class PluginUtils {

    private static final String PLUGIN_ID_PREFIX_TO_REMOVE = "org.gradle.";

    private static final Map<ClassLoader, Map<String, String>> PLUGIN_CLASS_NAMES = synchronizedMap(
        new WeakHashMap<>()
    );

    @SneakyThrows
    public static Map<String, String> getAllPluginClassNames(@Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getSystemClassLoader();
        }
        return PLUGIN_CLASS_NAMES.computeIfAbsent(classLoader, PluginUtils::getAllPluginClassNamesFor);
    }

    public static Map<String, String> getAllPluginClassNames() {
        val callingClass = getCallingClass(2);
        return getAllPluginClassNames(callingClass.getClassLoader());
    }

    @SneakyThrows
    private static Map<String, String> getAllPluginClassNamesFor(ClassLoader classLoader) {
        Map<String, String> result = new TreeMap<>();

        val resourceDir = "META-INF/gradle-plugins";
        try (
            val scanResult = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .acceptPathsNonRecursive(resourceDir)
                .scan()
        ) {
            val pathPrefix = resourceDir + '/';
            val pathSuffix = ".properties";
            for (val resource : scanResult.getAllResources()) {
                val path = resource.getPath();
                String pluginId = path.substring(pathPrefix.length(), path.length() - pathSuffix.length());
                if (pluginId.startsWith(PLUGIN_ID_PREFIX_TO_REMOVE)) {
                    pluginId = pluginId.substring(PLUGIN_ID_PREFIX_TO_REMOVE.length());
                }
                if (isEmpty(pluginId)) {
                    continue;
                }

                val properties = new Properties();
                try (val inputStream = resource.open()) {
                    properties.load(inputStream);
                }
                val implementationClassName = properties.getProperty("implementation-class");
                if (isEmpty(implementationClassName)) {
                    continue;
                }

                result.putIfAbsent(pluginId, implementationClassName);
            }
        }

        return ImmutableMap.copyOf(result);
    }


    @Nullable
    public static String findPluginIdFor(Class<? extends Plugin<?>> pluginType) {
        val pluginClassNames = getAllPluginClassNames(pluginType.getClassLoader());
        for (val entry : pluginClassNames.entrySet()) {
            if (pluginType.getName().equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

}
