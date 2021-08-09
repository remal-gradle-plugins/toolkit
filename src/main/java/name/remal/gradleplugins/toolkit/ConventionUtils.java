package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import lombok.val;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.Convention;

public interface ConventionUtils {

    @SuppressWarnings("deprecation")
    static Convention getConvention(Object object) {
        return new DslObject(object).getConvention();
    }


    static <T> T addConventionPlugin(Object object, String pluginName, T pluginInstance) {
        getConvention(object).getPlugins().put(pluginName, pluginInstance);
        return pluginInstance;
    }

    static <T> T addConventionPlugin(Object object, T pluginInstance) {
        val type = unwrapGeneratedSubclass(pluginInstance.getClass());
        getConvention(object).getPlugins().put(type.getName(), pluginInstance);
        return pluginInstance;
    }

}
