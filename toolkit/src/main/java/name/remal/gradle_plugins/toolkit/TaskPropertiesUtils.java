package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants.getClassName;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.ThrowableUtils.unwrapReflectionException;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.getClassHierarchy;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.getPropertyNameForGetter;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isClassPresent;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isGetter;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isPackagePrivate;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isPrivate;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isStatic;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isSynthetic;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.makeAccessible;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import com.google.common.reflect.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.ClasspathNormalizer;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.CompileClasspathNormalizer;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Destroys;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectories;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskInputFilePropertyBuilder;
import org.gradle.api.tasks.TaskOutputFilePropertyBuilder;
import org.gradle.work.Incremental;
import org.gradle.work.NormalizeLineEndings;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class TaskPropertiesUtils {

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Annotation>> UNSUPPORTED_ANNOTATIONS = Stream.of(
            getClassName(Incremental.class),
            getClassName(Destroys.class)
        )
        .map(className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        })
        .filter(Objects::nonNull)
        .map(clazz -> (Class<? extends Annotation>) clazz)
        .collect(toUnmodifiableList());

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Annotation>> INPUT_ANNOTATIONS = Stream.of(
            getClassName(Input.class),
            getClassName(InputDirectory.class),
            getClassName(InputFile.class),
            getClassName(InputFiles.class),
            getClassName(Nested.class)
        )
        .map(className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        })
        .filter(Objects::nonNull)
        .map(clazz -> (Class<? extends Annotation>) clazz)
        .collect(toUnmodifiableList());

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Annotation>> OUTPUT_ANNOTATIONS = Stream.of(
            getClassName(OutputDirectories.class),
            getClassName(OutputDirectory.class),
            getClassName(OutputFile.class),
            getClassName(OutputFiles.class)
        )
        .map(className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        })
        .filter(Objects::nonNull)
        .map(clazz -> (Class<? extends Annotation>) clazz)
        .collect(toUnmodifiableList());

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Annotation>> IGNORE_ANNOTATIONS = Stream.of(
            getClassName(Internal.class),
            getClassName(Console.class)
        )
        .map(className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        })
        .filter(Objects::nonNull)
        .map(clazz -> (Class<? extends Annotation>) clazz)
        .collect(toUnmodifiableList());

    private static final List<Class<? extends Annotation>> ALL_ANNOTATIONS = Stream.of(
        UNSUPPORTED_ANNOTATIONS,
        INPUT_ANNOTATIONS,
        OUTPUT_ANNOTATIONS,
        IGNORE_ANNOTATIONS
    ).flatMap(Collection::stream).collect(toUnmodifiableList());

    public static void registerTaskProperties(
        Task task,
        Object propertiesContainer
    ) {
        registerTaskProperties(task, propertiesContainer, null);
    }

    public static void registerTaskProperties(
        Task task,
        Object propertiesContainer,
        @Nullable String propertyNamePrefix
    ) {
        registerTaskPropertiesImpl(
            task,
            task.getProject().getProviders().provider(() -> propertiesContainer),
            unwrapGeneratedSubclass(propertiesContainer.getClass()),
            propertyNamePrefix
        );
    }

    private static void registerTaskPropertiesImpl(
        Task task,
        Provider<Object> propertiesContainerProvider,
        Class<?> propertiesContainerType,
        @Nullable String propertyNamePrefix
    ) {
        var propertiesContainerClassHierarchy = getClassHierarchy(propertiesContainerType);
        checkFieldsOf(propertiesContainerClassHierarchy);

        var candidateMethods = getCandidateMethodsOf(propertiesContainerClassHierarchy);
        if (candidateMethods.isEmpty()) {
            throw new TaskPropertiesException(format(
                "%s couldn't find any property-candidate methods in %s",
                TaskPropertiesUtils.class,
                propertiesContainerType
            ));
        }
        checkCandidateMethods(candidateMethods);

        for (var method : candidateMethods) {
            registerTaskPropertiesImpl(
                task,
                propertiesContainerProvider,
                propertiesContainerType,
                propertyNamePrefix,
                method
            );
        }
    }

    @SuppressWarnings({"unchecked", "java:S6541", "java:S3776"})
    private static void registerTaskPropertiesImpl(
        Task task,
        Provider<Object> propertiesContainerProvider,
        Class<?> propertiesContainerType,
        @Nullable String propertyNamePrefix,
        Method method
    ) {
        var propertyName = getPropertyNameForGetter(method);
        var fullPropertyName = isNotEmpty(propertyNamePrefix)
            ? propertyNamePrefix + "." + propertyName
            : propertyName;

        var propertyValueProvider = propertiesContainerProvider.map(propertiesContainer -> {
            try {
                return makeAccessible(method).invoke(propertiesContainer);
            } catch (Throwable e) {
                throw new TaskPropertiesException(
                    format(
                        "Error calling `%s` on `%s` (expected properties container type: `%s`)",
                        method,
                        propertiesContainer.getClass().getName(),
                        propertiesContainerType
                    ),
                    unwrapReflectionException(e)
                );
            }
        });

        if (method.isAnnotationPresent(Input.class)) {
            task.getInputs()
                .property(fullPropertyName, propertyValueProvider)
                .optional(method.isAnnotationPresent(org.gradle.api.tasks.Optional.class));

        } else if (method.isAnnotationPresent(InputDirectory.class)
            || method.isAnnotationPresent(InputFile.class)
            || method.isAnnotationPresent(InputFiles.class)
        ) {
            final TaskInputFilePropertyBuilder property;
            if (method.isAnnotationPresent(InputDirectory.class)) {
                property = task.getInputs().dir(propertyValueProvider);
            } else if (method.isAnnotationPresent(InputFile.class)) {
                property = task.getInputs().file(propertyValueProvider);
            } else if (method.isAnnotationPresent(InputFiles.class)) {
                property = task.getInputs().files(propertyValueProvider);
            } else {
                throw new TaskPropertiesException("Unsupported method: " + method);
            }
            property.withPropertyName(fullPropertyName)
                .skipWhenEmpty(method.isAnnotationPresent(SkipWhenEmpty.class))
                .optional(method.isAnnotationPresent(org.gradle.api.tasks.Optional.class));
            if (IGNORE_EMPTY_DIRECTORIES_PRESENT) {
                IgnoreEmptyDirectoriesCustomizer.customize(property, method);
            }
            if (NORMALIZE_LINE_ENDINGS_PRESENT) {
                NormalizeLineEndingsCustomizer.customize(property, method);
            }
            if (method.isAnnotationPresent(CompileClasspath.class)) {
                property.withNormalizer(CompileClasspathNormalizer.class);
            } else if (method.isAnnotationPresent(Classpath.class)) {
                property.withNormalizer(ClasspathNormalizer.class);
            }
            Optional.ofNullable(method.getAnnotation(PathSensitive.class))
                .map(PathSensitive::value)
                .ifPresent(property::withPathSensitivity);

        } else if (method.isAnnotationPresent(Nested.class)) {
            if (Provider.class.isAssignableFrom(method.getReturnType())) {
                var invokable = TypeToken.of(propertiesContainerType).method(method);
                @SuppressWarnings("rawtypes")
                var returnType = invokable.getReturnType().getSupertype((Class) Provider.class).getType();
                if (returnType instanceof ParameterizedType) {
                    var nestedType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
                    var nestedClass = TypeToken.of(nestedType).getRawType();
                    registerTaskPropertiesImpl(
                        task,
                        propertiesContainerProvider.flatMap(propertiesContainer -> {
                            try {
                                return (Provider<?>) makeAccessible(method).invoke(propertiesContainer);
                            } catch (Throwable e) {
                                throw new TaskPropertiesException(
                                    format(
                                        "Error calling `%s` on `%s` (expected properties container type: `%s`)",
                                        method,
                                        propertiesContainer.getClass().getName(),
                                        propertiesContainerType
                                    ),
                                    unwrapReflectionException(e)
                                );
                            }
                        }),
                        nestedClass,
                        fullPropertyName
                    );
                } else {
                    throw new TaskPropertiesException("Unsupported method: " + method);
                }

            } else {
                registerTaskPropertiesImpl(
                    task,
                    propertyValueProvider,
                    method.getReturnType(),
                    fullPropertyName
                );
            }

        } else if (method.isAnnotationPresent(OutputDirectories.class)
            || method.isAnnotationPresent(OutputDirectory.class)
            || method.isAnnotationPresent(OutputFile.class)
            || method.isAnnotationPresent(OutputFiles.class)
        ) {
            final TaskOutputFilePropertyBuilder property;
            if (method.isAnnotationPresent(OutputDirectories.class)) {
                property = task.getOutputs().dirs(propertyValueProvider);
            } else if (method.isAnnotationPresent(OutputDirectory.class)) {
                property = task.getOutputs().dir(propertyValueProvider);
            } else if (method.isAnnotationPresent(OutputFile.class)) {
                property = task.getOutputs().file(propertyValueProvider);
            } else if (method.isAnnotationPresent(OutputFiles.class)) {
                property = task.getOutputs().files(propertyValueProvider);
            } else {
                throw new TaskPropertiesException("Unsupported method: " + method);
            }
            property.withPropertyName(fullPropertyName)
                .optional(method.isAnnotationPresent(org.gradle.api.tasks.Optional.class));

        } else if (IGNORE_ANNOTATIONS.stream().anyMatch(method::isAnnotationPresent)) {
            // ignore

        } else {
            throw new TaskPropertiesException("Unsupported method: " + method);
        }
    }

    private static final boolean IGNORE_EMPTY_DIRECTORIES_PRESENT = isClassPresent(
        getClassName(IgnoreEmptyDirectories.class),
        TaskPropertiesUtils.class.getClassLoader()
    );

    private static class IgnoreEmptyDirectoriesCustomizer {
        public static void customize(TaskInputFilePropertyBuilder property, Method method) {
            property.ignoreEmptyDirectories(method.isAnnotationPresent(IgnoreEmptyDirectories.class));
        }
    }

    private static final boolean NORMALIZE_LINE_ENDINGS_PRESENT = isClassPresent(
        getClassName(NormalizeLineEndings.class),
        TaskPropertiesUtils.class.getClassLoader()
    );

    private static class NormalizeLineEndingsCustomizer {
        public static void customize(TaskInputFilePropertyBuilder property, Method method) {
            property.normalizeLineEndings(method.isAnnotationPresent(NormalizeLineEndings.class));
        }
    }

    private static void checkCandidateMethods(List<Method> candidateMethods) {
        candidateMethods.forEach(method -> {
            Stream.of(UNSUPPORTED_ANNOTATIONS)
                .flatMap(Collection::stream)
                .forEach(annotationType -> {
                    if (method.isAnnotationPresent(annotationType)) {
                        throw new TaskPropertiesException(format(
                            "%s doesn't support method annotated with %s: %s",
                            TaskPropertiesUtils.class,
                            annotationType,
                            method
                        ));
                    }
                });

            ALL_ANNOTATIONS.forEach(annotationType -> {
                if (method.isAnnotationPresent(annotationType)) {
                    if (isSynthetic(method)
                        || isStatic(method)
                        || isPrivate(method)
                        || isPackagePrivate(method)
                    ) {
                        throw new TaskPropertiesException(format(
                            "%s doesn't support method annotated with %s: %s",
                            TaskPropertiesUtils.class,
                            annotationType,
                            method
                        ));
                    }

                    if (!isGetter(method)) {
                        throw new TaskPropertiesException(format(
                            "%s doesn't support method annotated with %s because it's not a getter: %s",
                            TaskPropertiesUtils.class,
                            annotationType,
                            method
                        ));
                    }
                }
            });
        });
    }

    private static List<Method> getCandidateMethodsOf(
        List<Class<?>> propertiesContainerClassHierarchy
    ) {
        return propertiesContainerClassHierarchy.stream()
            .filter(not(Object.class::equals))
            .map(Class::getDeclaredMethods)
            .flatMap(Arrays::stream)
            .filter(method -> ALL_ANNOTATIONS.stream().anyMatch(method::isAnnotationPresent))
            .collect(toUnmodifiableList());
    }

    private static void checkFieldsOf(
        List<Class<?>> propertiesContainerClassHierarchy
    ) {
        propertiesContainerClassHierarchy.stream()
            .filter(not(Object.class::equals))
            .map(Class::getDeclaredFields)
            .flatMap(Arrays::stream)
            .forEach(field -> {
                ALL_ANNOTATIONS.forEach(annotationType -> {
                    if (field.isAnnotationPresent(annotationType)) {
                        throw new TaskPropertiesException(format(
                            "%s doesn't support field annotated with %s: %s",
                            TaskPropertiesUtils.class,
                            annotationType,
                            field
                        ));
                    }
                });
            });
    }

}
