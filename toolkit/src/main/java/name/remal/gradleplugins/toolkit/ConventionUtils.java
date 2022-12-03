package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradleplugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.Convention;

@ReliesOnInternalGradleApi
@NoArgsConstructor(access = PRIVATE)
public abstract class ConventionUtils {

    @SuppressWarnings("deprecation")
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
