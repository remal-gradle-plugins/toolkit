package name.remal.gradle_plugins.toolkit;

import javax.annotation.Nullable;
import org.gradle.api.plugins.ExtensionContainer;

interface ExtensionContainerUtilsMethods {

    @Nullable
    <T> T findConventionPlugin(ExtensionContainer extensions, Class<T> type);

    @Nullable
    Object findConventionPlugin(ExtensionContainer extensions, String name);

}
