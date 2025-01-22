package name.remal.gradle_plugins.toolkit;

import static java.lang.Character.toUpperCase;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;
import static name.remal.gradle_plugins.toolkit.ThrowableUtils.unwrapReflectionException;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.getClassHierarchy;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.getPropertyNameForGetter;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isAbstract;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isFinal;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isGetter;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isIndependentClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isNotAbstract;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isPrivate;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isRecord;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.makeAccessible;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.streamClassHierarchyWithoutInterfaces;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapPrimitiveType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils;
import name.remal.gradle_plugins.toolkit.reflection.TypedVoidMethod1;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.HasMultipleValues;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Nested;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleManagedObjectsUtils {

    @Nullable
    private static final TypedVoidMethod1<ConfigurableFileCollection, Object[]> CONFIGURABLE_FILE_COLLECTION_CONVENTION
        = findMethod(ConfigurableFileCollection.class, "convention", Object[].class);

    @SuppressWarnings(
        {
            "rawtypes", "unchecked",
            "LoopStatementThatDoesntLoop",
            "java:S3776", "java:S6541", "java:S1751"
        }
    )
    public static <PARENT, SOURCE extends PARENT, TARGET extends PARENT> void copyManagedProperties(
        SOURCE source,
        TARGET target
    ) {
        var sourceType = source.getClass();
        var targetType = target.getClass();
        Class parentType = null;
        if (sourceType.isAssignableFrom(targetType)) {
            parentType = sourceType;
        } else if (targetType.isAssignableFrom(sourceType)) {
            parentType = targetType;
        }

        noParentType:
        while (parentType == null) {
            var sourceHierarchy = getClassHierarchy(sourceType);
            for (var clazz : sourceHierarchy) {
                if (clazz != Object.class
                    && !clazz.isInterface()
                    && clazz.isAssignableFrom(targetType)
                ) {
                    parentType = clazz;
                    break noParentType;
                }
            }

            var targetHierarchy = getClassHierarchy(sourceType);
            for (var clazz : targetHierarchy) {
                if (clazz != Object.class
                    && !clazz.isInterface()
                    && clazz.isAssignableFrom(sourceType)
                ) {
                    parentType = clazz;
                    break noParentType;
                }
            }

            for (var clazz : sourceHierarchy) {
                if (clazz.isInterface()
                    && clazz.isAssignableFrom(targetType)
                ) {
                    parentType = clazz;
                    break noParentType;
                }
            }

            for (var clazz : targetHierarchy) {
                if (clazz.isInterface()
                    && clazz.isAssignableFrom(sourceType)
                ) {
                    parentType = clazz;
                    break noParentType;
                }
            }

            break;
        }

        if (parentType == null
            || parentType == Object.class
        ) {
            throw new IllegalStateException(format(
                "Can't copy managed Gradle properties, as common type (not Object) was not found."
                    + " Source  type: %s."
                    + " Target  type: %s.",
                sourceType,
                targetType
            ));
        }

        copyManagedProperties(
            parentType,
            source,
            target
        );
    }

    @SneakyThrows
    @SuppressWarnings({"unchecked", "rawtypes", "java:S3776", "java:S6541"})
    public static <PARENT, SOURCE extends PARENT, TARGET extends PARENT> void copyManagedProperties(
        Class<PARENT> parentType,
        SOURCE source,
        TARGET target
    ) {
        if (!parentType.isInstance(source)) {
            throw new IllegalArgumentException(format("%s is not instance of %s", source, parentType));
        }
        if (!parentType.isInstance(target)) {
            throw new IllegalArgumentException(format("%s is not instance of %s", target, parentType));
        }

        var managedGetters = new LinkedHashMap<String, Method>();
        getClassHierarchy(parentType).stream()
            .filter(not(Object.class::equals))
            .map(Class::getDeclaredMethods)
            .flatMap(Arrays::stream)
            .filter(GradleManagedObjectsUtils::isGradleManagedPropertyGetter)
            .forEach(method -> managedGetters.putIfAbsent(method.getName(), makeAccessible(method)));
        for (var getter : managedGetters.values()) {
            final Object sourceValue;
            final Object targetValue;
            try {
                sourceValue = getter.invoke(source);
                targetValue = getter.invoke(target);
            } catch (Throwable e) {
                throw unwrapReflectionException(e);
            }

            if (sourceValue == null
                || targetValue == null
                || sourceValue == targetValue
            ) {
                continue;
            }

            if (sourceValue instanceof Property) {
                ((Property) targetValue).convention((Provider) sourceValue);

            } else if (sourceValue instanceof HasMultipleValues) {
                ((HasMultipleValues) targetValue).convention((Provider) sourceValue);

            } else if (sourceValue instanceof MapProperty) {
                ((MapProperty) targetValue).convention((Provider) sourceValue);

            } else if (sourceValue instanceof ConfigurableFileCollection) {
                if (CONFIGURABLE_FILE_COLLECTION_CONVENTION != null) {
                    CONFIGURABLE_FILE_COLLECTION_CONVENTION.invoke(
                        (ConfigurableFileCollection) targetValue,
                        new Object[]{sourceValue}
                    );
                } else {
                    ((ConfigurableFileCollection) targetValue).from(sourceValue);
                }

            } else if (sourceValue instanceof ConfigurableFileTree) {
                // not supported

            } else if (sourceValue instanceof String) {
                // not supported

            } else {
                copyManagedProperties(sourceValue, targetValue);
            }
        }
    }

    /**
     * See
     * <a href="https://docs.gradle.org/current/userguide/properties_providers.html#managed_properties">https://docs.gradle.org/current/userguide/properties_providers.html#managed_properties</a>.
     */
    public static boolean isGradleManagedPropertyGetter(Method method) {
        if (isNotAbstract(method)
            || isPrivate(method)
            || !isGetter(method)
        ) {
            return false;
        }

        var type = method.getReturnType();
        if (type == Property.class
            || type == RegularFileProperty.class
            || type == DirectoryProperty.class
            || type == ListProperty.class
            || type == SetProperty.class
            || type == MapProperty.class
            || type == ConfigurableFileCollection.class
            || type == ConfigurableFileTree.class
        ) {
            return true;
        }

        if (type == String.class) {
            return method.getName().equals("getName");
        }

        if (unwrapPrimitiveType(type).isPrimitive()) {
            return false;
        }

        var isAnnotatedWithNested = method.isAnnotationPresent(Nested.class);
        if (isAnnotatedWithNested) {
            var propertyName = getPropertyNameForGetter(method);
            var setterName = "set" + toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            var hasSetter = streamClassHierarchyWithoutInterfaces(method.getDeclaringClass())
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(ReflectionUtils::isNotPrivate)
                .filter(curMethod -> curMethod.getParameterCount() == 1)
                .anyMatch(curMethod -> curMethod.getName().equals(setterName));
            return !hasSetter;
        }

        return false;
    }

    /**
     * See
     * <a href="https://docs.gradle.org/current/userguide/properties_providers.html#managed_types">https://docs.gradle.org/current/userguide/properties_providers.html#managed_types</a>.
     */
    public static boolean isGradleManagedType(Class<?> clazz) {
        if (isPrivate(clazz)
            || isFinal(clazz)
            || unwrapPrimitiveType(clazz).isPrimitive()
            || clazz.isArray()
            || clazz.isAnnotation()
            || clazz.isEnum()
            || isRecord(clazz)
            || clazz == Object.class
        ) {
            return false;
        }

        if (!isIndependentClass(clazz)) {
            return false;
        }
        if (!clazz.isInterface() && !isAbstract(clazz)) {
            return false;
        }

        var hasFields = getClassHierarchy(clazz).stream()
            .map(Class::getDeclaredFields)
            .flatMap(Arrays::stream)
            .filter(ReflectionUtils::isNotSynthetic)
            .anyMatch(ReflectionUtils::isNotStatic);
        if (hasFields) {
            return false;
        }

        var allMethodsAreManagedGetters = getClassHierarchy(clazz).stream()
            .filter(not(Object.class::equals))
            .map(Class::getDeclaredMethods)
            .flatMap(Arrays::stream)
            .filter(ReflectionUtils::isNotSynthetic)
            .filter(ReflectionUtils::isNotStatic)
            .filter(ReflectionUtils::isNotPrivate)
            .allMatch(GradleManagedObjectsUtils::isGradleManagedPropertyGetter);
        if (!allMethodsAreManagedGetters) {
            return false;
        }

        if (clazz.isInterface()) {
            return true;
        }

        for (var ctor : clazz.getDeclaredConstructors()) {
            if (isPrivate(ctor)) {
                continue;
            }

            if (ctor.getParameterCount() == 0
                || ctor.isAnnotationPresent(Inject.class)
            ) {
                return true;
            }
        }

        return false;
    }

}
