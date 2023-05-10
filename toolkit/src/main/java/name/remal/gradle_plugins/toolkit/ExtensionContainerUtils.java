package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import java.util.Locale;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

@NoArgsConstructor(access = PRIVATE)
public abstract class ExtensionContainerUtils {

    public static ExtensionContainer getExtensions(Object object) {
        return ((ExtensionAware) object).getExtensions();
    }


    public static <T> T createExtension(
        Object object,
        Class<T> publicType,
        String name,
        Class<? extends T> instanceType,
        Object... constructionArguments
    ) {
        val extensions = getExtensions(object);
        return extensions.create(publicType, name, instanceType, constructionArguments);
    }

    public static <T> T createExtension(
        Object object,
        Class<T> publicType,
        Class<? extends T> instanceType,
        Object... constructionArguments
    ) {
        val name = typeToExtensionName(publicType);
        return createExtension(object, publicType, name, instanceType, constructionArguments);
    }

    public static <T> T createExtension(
        Object object,
        String name,
        Class<T> type,
        Object... constructionArguments
    ) {
        return createExtension(object, type, name, type, constructionArguments);
    }

    public static <T> T createExtension(
        Object object,
        Class<T> type,
        Object... constructionArguments
    ) {
        val name = typeToExtensionName(type);
        return createExtension(object, name, type, constructionArguments);
    }


    public static <T, I extends T> T addExtension(
        Object object,
        Class<T> publicType,
        String name,
        I instance
    ) {
        val extensions = getExtensions(object);
        extensions.add(publicType, name, instance);
        return instance;
    }

    public static <T> T addExtension(
        Object object,
        String name,
        T instance
    ) {
        val extensions = getExtensions(object);
        extensions.add(name, instance);
        return instance;
    }

    public static <T, I extends T> T addExtension(
        Object object,
        Class<T> publicType,
        I instance
    ) {
        val name = typeToExtensionName(publicType);
        return addExtension(object, publicType, name, instance);
    }

    public static <T> T addExtension(
        Object object,
        T instance
    ) {
        val name = typeToExtensionName(instance.getClass());
        return addExtension(object, name, instance);
    }

    private static String typeToExtensionName(Class<?> type) {
        type = unwrapGeneratedSubclass(type);
        val simpleName = type.getSimpleName();
        return simpleName.substring(0, 1).toLowerCase(Locale.ROOT) + simpleName.substring(1);
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
    private static <T> T findConventionPlugin(ExtensionContainer extensions, Class<T> type) {
        if (extensions instanceof Convention) {
            return ((Convention) extensions).findPlugin(type);
        }
        return null;
    }

    @Nullable
    private static Object findConventionPlugin(ExtensionContainer extensions, String name) {
        if (extensions instanceof Convention) {
            return ((Convention) extensions).getPlugins().get(name);
        }
        return null;
    }

}
