package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.reflection.ClassUtils.tryLoadClass;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.findMethod;

import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradleplugins.toolkit.reflection.TypedMethod0;
import name.remal.gradleplugins.toolkit.reflection.TypedMethod1;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

public abstract class ExtensionContainerUtils {

    public static ExtensionContainer getExtensions(Object object) {
        return ((ExtensionAware) object).getExtensions();
    }


    public static boolean hasExtension(Object object, Class<?> type) {
        return findExtension(object, type) != null;
    }

    public static boolean hasExtension(Object object, String name) {
        return findExtension(object, name) != null;
    }

    @Nullable
    public static <T> T findExtension(Object object, Class<T> type) {
        val extensions = getExtensions(object);
        val conventionPlugin = findConventionPlugin(extensions, type);
        if (conventionPlugin != null) {
            return conventionPlugin;
        }

        return extensions.findByType(type);
    }

    @Nullable
    public static Object findExtension(Object object, String name) {
        val extensions = getExtensions(object);
        val conventionPlugin = findConventionPlugin(extensions, name);
        if (conventionPlugin != null) {
            return conventionPlugin;
        }

        return extensions.findByName(name);
    }

    public static <T> T getExtension(Object object, Class<T> type) {
        val extensions = getExtensions(object);
        val conventionPlugin = findConventionPlugin(extensions, type);
        if (conventionPlugin != null) {
            return conventionPlugin;
        }

        return extensions.getByType(type);
    }

    public static Object getExtension(Object object, String name) {
        val extensions = getExtensions(object);
        val conventionPlugin = findConventionPlugin(extensions, name);
        if (conventionPlugin != null) {
            return conventionPlugin;
        }

        return extensions.getByName(name);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static final Class<Object> CONVENTION_CLASS = (Class<Object>) tryLoadClass(
        "org.gradle.api.plugins.Convention"
    );

    @Nullable
    @SuppressWarnings("rawtypes")
    private static final TypedMethod1<Object, Object, Class> CONVENTION_FIND_PLUGIN_METHOD =
        Optional.ofNullable(CONVENTION_CLASS)
            .map(conventionClass ->
                findMethod(conventionClass, Object.class, "findPlugin", Class.class)
            )
            .orElse(null);

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> T findConventionPlugin(ExtensionContainer extensions, Class<T> type) {
        if (CONVENTION_CLASS != null
            && CONVENTION_FIND_PLUGIN_METHOD != null
            && CONVENTION_CLASS.isInstance(extensions)
        ) {
            val plugin = CONVENTION_FIND_PLUGIN_METHOD.invoke(extensions, type);
            if (plugin != null) {
                return (T) plugin;
            }
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("rawtypes")
    private static final TypedMethod0<Object, Map> CONVENTION_GET_PLUGINS_METHOD =
        Optional.ofNullable(CONVENTION_CLASS)
            .map(conventionClass ->
                findMethod(conventionClass, Map.class, "getPlugins")
            )
            .orElse(null);

    @Nullable
    private static Object findConventionPlugin(ExtensionContainer extensions, String name) {
        if (CONVENTION_CLASS != null
            && CONVENTION_GET_PLUGINS_METHOD != null
            && CONVENTION_CLASS.isInstance(extensions)
        ) {
            val plugins = CONVENTION_GET_PLUGINS_METHOD.invoke(extensions);
            if (plugins != null) {
                return plugins.get(name);
            }
        }
        return null;
    }


    private ExtensionContainerUtils() {
    }

}
