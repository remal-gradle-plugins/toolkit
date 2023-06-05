package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;
import org.gradle.api.plugins.ExtensionContainer;

@AutoService(ExtensionContainerUtilsMethods.class)
class ExtensionContainerUtilsMethodsDefault implements ExtensionContainerUtilsMethods {

    @Nullable
    @Override
    public <T> T findConventionPlugin(ExtensionContainer extensions, Class<T> type) {
        return null;
    }

    @Nullable
    @Override
    public Object findConventionPlugin(ExtensionContainer extensions, String name) {
        return null;
    }

}
