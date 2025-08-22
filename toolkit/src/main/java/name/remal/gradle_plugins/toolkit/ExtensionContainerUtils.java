package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
import static org.gradle.api.reflect.TypeOf.typeOf;

import java.util.Locale;
import java.util.Optional;
import lombok.NoArgsConstructor;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.reflect.TypeOf;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class ExtensionContainerUtils {

    public static ExtensionContainer getExtensions(Object object) {
        return ((ExtensionAware) object).getExtensions();
    }


    public static <T> T createExtension(
        Object object,
        TypeOf<T> publicType,
        String name,
        Class<? extends T> instanceType,
        Object... constructionArguments
    ) {
        var extensions = getExtensions(object);
        return extensions.create(publicType, name, instanceType, constructionArguments);
    }

    public static <T> T createExtension(
        Object object,
        Class<T> publicType,
        String name,
        Class<? extends T> instanceType,
        Object... constructionArguments
    ) {
        return createExtension(object, typeOf(publicType), name, instanceType, constructionArguments);
    }

    public static <T> T createExtension(
        Object object,
        TypeOf<T> publicType,
        Class<? extends T> instanceType,
        Object... constructionArguments
    ) {
        var name = typeToExtensionName(publicType.getConcreteClass());
        return createExtension(object, publicType, name, instanceType, constructionArguments);
    }

    public static <T> T createExtension(
        Object object,
        Class<T> publicType,
        Class<? extends T> instanceType,
        Object... constructionArguments
    ) {
        return createExtension(object, typeOf(publicType), instanceType, constructionArguments);
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
        var name = typeToExtensionName(type);
        return createExtension(object, name, type, constructionArguments);
    }


    public static <T, I extends T> T addExtension(
        Object object,
        TypeOf<T> publicType,
        String name,
        I instance
    ) {
        var extensions = getExtensions(object);
        extensions.add(publicType, name, instance);
        return instance;
    }

    public static <T, I extends T> T addExtension(
        Object object,
        Class<T> publicType,
        String name,
        I instance
    ) {
        return addExtension(object, typeOf(publicType), name, instance);
    }

    public static <T> T addExtension(
        Object object,
        String name,
        T instance
    ) {
        var extensions = getExtensions(object);
        extensions.add(name, instance);
        return instance;
    }

    public static <T, I extends T> T addExtension(
        Object object,
        TypeOf<T> publicType,
        I instance
    ) {
        var name = typeToExtensionName(publicType.getConcreteClass());
        return addExtension(object, publicType, name, instance);
    }

    public static <T, I extends T> T addExtension(
        Object object,
        Class<T> publicType,
        I instance
    ) {
        return addExtension(object, typeOf(publicType), instance);
    }

    public static <T> T addExtension(
        Object object,
        T instance
    ) {
        var name = typeToExtensionName(instance.getClass());
        return addExtension(object, name, instance);
    }

    private static String typeToExtensionName(Class<?> type) {
        type = unwrapGeneratedSubclass(type);
        var simpleName = type.getSimpleName();
        return simpleName.substring(0, 1).toLowerCase(Locale.ROOT) + simpleName.substring(1);
    }


    public static boolean hasExtension(Object object, TypeOf<?> type) {
        return findExtension(object, type) != null;
    }

    public static boolean hasExtension(Object object, Class<?> type) {
        return hasExtension(object, typeOf(type));
    }

    public static boolean hasExtension(Object object, String name) {
        return findExtension(object, name) != null;
    }

    @Nullable
    public static <T> T findExtension(Object object, TypeOf<T> type) {
        var extensions = getExtensions(object);
        return extensions.findByType(type);
    }

    @Nullable
    public static <T> T findExtension(Object object, Class<T> type) {
        return findExtension(object, typeOf(type));
    }

    @Nullable
    public static Object findExtension(Object object, String name) {
        var extensions = getExtensions(object);
        return extensions.findByName(name);
    }

    public static <T> T getExtension(Object object, TypeOf<T> type) {
        var extensions = getExtensions(object);
        return extensions.getByType(type);
    }

    public static <T> T getExtension(Object object, Class<T> type) {
        return getExtension(object, typeOf(type));
    }

    public static Object getExtension(Object object, String name) {
        var extensions = getExtensions(object);
        return extensions.getByName(name);
    }

    public static <T> Optional<T> getOptionalExtension(Object object, TypeOf<T> type) {
        return Optional.ofNullable(findExtension(object, type));
    }

    public static <T> Optional<T> getOptionalExtension(Object object, Class<T> type) {
        return getOptionalExtension(object, typeOf(type));
    }

}
