package name.remal.gradleplugins.toolkit;

import static java.lang.ClassLoader.getSystemClassLoader;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradleplugins.toolkit.reflection.WhoCalledUtils.getCallingClass;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.github.classgraph.ClassGraph;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Plugin;

@NoArgsConstructor(access = PRIVATE)
public abstract class PluginUtils {

    private static final LoadingCache<ClassLoader, Map<String, String>> PLUGIN_CLASS_NAMES = CacheBuilder.newBuilder()
        .weakKeys()
        .build(new CacheLoader<ClassLoader, Map<String, String>>() {
            @Override
            public Map<String, String> load(ClassLoader classLoader) throws Exception {
                Map<String, String> result = new LinkedHashMap<>();

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
                        val pluginId = path.substring(pathPrefix.length(), path.length() - pathSuffix.length());
                        val properties = new Properties();
                        try (val inputStream = resource.open()) {
                            properties.load(inputStream);
                        }
                        val implementationClassName = properties.getProperty("implementation-class");
                        if (isNotEmpty(pluginId) && isNotEmpty(implementationClassName)) {
                            result.putIfAbsent(pluginId, implementationClassName);
                        }
                    }
                }

                return ImmutableMap.copyOf(result);
            }
        });

    @SneakyThrows
    public static Map<String, String> getAllPluginClassNames(@Nullable ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getSystemClassLoader();
        }
        return PLUGIN_CLASS_NAMES.get(classLoader);
    }

    public static Map<String, String> getAllPluginClassNames() {
        val callingClass = getCallingClass(2);
        return getAllPluginClassNames(callingClass.getClassLoader());
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
