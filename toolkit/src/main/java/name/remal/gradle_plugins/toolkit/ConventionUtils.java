package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.Convention;

/**
 * @deprecated {@link Convention} concept is deprecated in Gradle 8.2.
 */
@Deprecated
@ReliesOnInternalGradleApi
@NoArgsConstructor(access = PRIVATE)
public abstract class ConventionUtils {

    public static Convention getConvention(Object object) {
        return new DslObject(object).getConvention();
    }


    public static <T> T addConventionPlugin(Object object, String pluginName, T pluginInstance) {
        getConvention(object).getPlugins().put(pluginName, pluginInstance);
        return pluginInstance;
    }

    public static <T> T addConventionPlugin(Object object, T pluginInstance) {
        val type = unwrapGeneratedSubclass(pluginInstance.getClass());
        getConvention(object).getPlugins().put(type.getName(), pluginInstance);
        return pluginInstance;
    }

}
