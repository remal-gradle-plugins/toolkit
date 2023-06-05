package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;

@AutoService(ExtensionContainerUtilsMethods.class)
class ExtensionContainerUtilsMethods_8_1 implements ExtensionContainerUtilsMethods {

    @Nullable
    @Override
    public <T> T findConventionPlugin(ExtensionContainer extensions, Class<T> type) {
        if (extensions instanceof Convention) {
            return ((Convention) extensions).findPlugin(type);
        }
        return null;
    }

    @Nullable
    @Override
    public Object findConventionPlugin(ExtensionContainer extensions, String name) {
        if (extensions instanceof Convention) {
            return ((Convention) extensions).getPlugins().get(name);
        }
        return null;
    }

}
