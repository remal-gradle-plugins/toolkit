package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadAllCrossCompileServiceImplementations;
import static name.remal.gradle_plugins.toolkit.FileTreeElementUtils.isNotArchiveEntry;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyListProxy;
import static name.remal.gradle_plugins.toolkit.ThrowableUtils.unwrapReflectionException;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isGetterOf;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils;
import name.remal.gradle_plugins.toolkit.reflection.TypedMethod0;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.jetbrains.annotations.Unmodifiable;

@NoArgsConstructor(access = PRIVATE)
public abstract class SourceSetUtils {

    private static final Pattern GET_CONFIGURATION_NAME_METHOD_NAME = Pattern.compile(
        "^get[A-Z].*[a-z]ConfigurationName$"
    );

    private static final List<Method> GET_CONFIGURATION_NAME_METHODS = Stream.of(SourceSet.class.getMethods())
        .filter(ReflectionUtils::isNotStatic)
        .filter(it -> it.getParameterCount() == 0)
        .filter(it -> it.getReturnType() == String.class)
        .filter(it -> GET_CONFIGURATION_NAME_METHOD_NAME.matcher(it.getName()).matches())
        .sorted(comparing(Method::getName))
        .collect(toUnmodifiableList());

    @SneakyThrows
    public static Set<String> getSourceSetConfigurationNames(SourceSet sourceSet) {
        Set<String> configurationNames = new LinkedHashSet<>();
        for (var method : GET_CONFIGURATION_NAME_METHODS) {
            final Object nameObject;
            try {
                nameObject = method.invoke(sourceSet);
            } catch (Throwable e) {
                throw unwrapReflectionException(e);
            }

            if (nameObject != null) {
                var name = nameObject.toString();
                if (!name.isEmpty()) {
                    configurationNames.add(name);
                }
            }
        }
        return configurationNames;
    }

    public static boolean isSourceSetConfigurationName(SourceSet sourceSet, String configurationName) {
        return getSourceSetConfigurationNames(sourceSet).contains(configurationName);
    }

    public static boolean isSourceSetConfiguration(SourceSet sourceSet, Configuration configuration) {
        var configurationName = configuration.getName();
        return isSourceSetConfigurationName(sourceSet, configurationName);
    }


    public static boolean isProcessedBy(SourceSet sourceSet, SourceTask task) {
        var result = new AtomicBoolean(false);
        var allSource = sourceSet.getAllSource();
        task.getSource().visit(details -> {
            if (isNotArchiveEntry(details)) {
                var file = details.getFile();
                if (allSource.contains(file)) {
                    result.set(true);
                    details.stopVisiting();
                }
            }
        });
        return result.get();
    }

    public static boolean isProcessedBy(SourceSet sourceSet, AbstractCopyTask task) {
        var result = new AtomicBoolean(false);
        var allSource = sourceSet.getAllSource();
        task.getSource().getAsFileTree().visit(details -> {
            if (isNotArchiveEntry(details)) {
                var file = details.getFile();
                if (allSource.contains(file)) {
                    result.set(true);
                    details.stopVisiting();
                }
            }
        });
        return result.get();
    }

    public static boolean isProcessedBy(SourceSet sourceSet, Task task) {
        if (task instanceof SourceTask) {
            return isProcessedBy(sourceSet, (SourceTask) task);
        } else if (task instanceof AbstractCopyTask) {
            return isProcessedBy(sourceSet, (AbstractCopyTask) task);
        } else {
            return false;
        }
    }


    public static boolean isCompiledBy(SourceSet sourceSet, AbstractCompile task) {
        var destinationDir = task.getDestinationDirectory().getAsFile().getOrNull();
        if (destinationDir == null) {
            return isProcessedBy(sourceSet, task);
        }

        return sourceSet.getOutput().getClassesDirs().contains(destinationDir);
    }

    @SneakyThrows
    private static boolean isCompiledByKotlin(SourceSet sourceSet, Task task) {
        var taskClass = unwrapGeneratedSubclass(task.getClass());
        var destinationDirectoryMethod = DESTINATION_DIRECTORY_METHODS_CACHE.get(taskClass.getClassLoader())
            .orElse(null);
        if (destinationDirectoryMethod == null) {
            return false;
        }

        if (!destinationDirectoryMethod.getReflectionMethod().getDeclaringClass().isInstance(task)) {
            return false;
        }

        var destinationDir = destinationDirectoryMethod.invoke(task).getAsFile().getOrNull();
        if (destinationDir == null) {
            return false;
        }

        return sourceSet.getOutput().getClassesDirs().contains(destinationDir);
    }

    private static final LoadingCache<
        ClassLoader,
        Optional<TypedMethod0<Object, DirectoryProperty>>
        > DESTINATION_DIRECTORY_METHODS_CACHE =
        CacheBuilder.newBuilder()
            .weakKeys()
            .build(CacheLoader.from(SourceSetUtils::findDestinationDirectoryMethod));

    @SuppressWarnings("unchecked")
    private static Optional<TypedMethod0<Object, DirectoryProperty>> findDestinationDirectoryMethod(
        ClassLoader classLoader
    ) {
        final Class<?> kotlinCompileToolClass;
        try {
            kotlinCompileToolClass = Class.forName(
                "org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool",
                false,
                classLoader
            );
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }

        var destinationDirectoryMethod = findMethod(
            (Class<Object>) kotlinCompileToolClass,
            DirectoryProperty.class,
            "getDestinationDirectory"
        );
        return Optional.ofNullable(destinationDirectoryMethod);
    }

    public static boolean isCompiledBy(SourceSet sourceSet, Task task) {
        if (task instanceof AbstractCompile) {
            return isCompiledBy(sourceSet, (AbstractCompile) task);
        } else {
            return isCompiledByKotlin(sourceSet, task);
        }
    }


    private static final Pattern GET_TASK_NAME_METHOD_NAME = Pattern.compile(
        "^get[A-Z].*[a-z]TaskName$"
    );

    private static final List<Method> GET_TASK_NAME_METHODS = Stream.of(SourceSet.class.getMethods())
        .filter(ReflectionUtils::isNotStatic)
        .filter(method -> isGetterOf(method, String.class))
        .filter(it -> GET_TASK_NAME_METHOD_NAME.matcher(it.getName()).matches())
        .sorted(comparing(Method::getName))
        .collect(toUnmodifiableList());

    @SneakyThrows
    public static boolean isSourceSetTask(SourceSet sourceSet, Task task) {
        for (var method : GET_TASK_NAME_METHODS) {
            final Object nameObject;
            try {
                nameObject = method.invoke(sourceSet);
            } catch (Throwable e) {
                throw unwrapReflectionException(e);
            }

            if (nameObject != null) {
                var name = nameObject.toString();
                if (!name.isEmpty()) {
                    if (task.getName().equals(name)) {
                        return true;
                    }
                }
            }
        }

        return isCompiledBy(sourceSet, task)
            || isProcessedBy(sourceSet, task);
    }


    private static final List<SourceSetSourceDirectorySetsGetter> SOURCE_SET_SOURCE_DIRECTORY_SETS_GETTERS =
        asLazyListProxy(() ->
            loadAllCrossCompileServiceImplementations(SourceSetSourceDirectorySetsGetter.class)
        );

    @Unmodifiable
    @SneakyThrows
    public static Collection<SourceDirectorySet> getAllSourceDirectorySets(SourceSet sourceSet) {
        Collection<SourceDirectorySet> result = newSetFromMap(new IdentityHashMap<>());
        for (var getter : SOURCE_SET_SOURCE_DIRECTORY_SETS_GETTERS) {
            try {
                var sourceDirectorySetsCollection = getter.get(sourceSet);
                result.addAll(sourceDirectorySetsCollection);
            } catch (Throwable e) {
                throw unwrapReflectionException(e);
            }
        }
        return unmodifiableCollection(result);
    }


    private static final List<WhenTestSourceSetRegistered> ALL_WHEN_TEST_SOURCE_SET_REGISTERED = asLazyListProxy(
        () -> loadAllCrossCompileServiceImplementations(WhenTestSourceSetRegistered.class)
    );

    public static void whenTestSourceSetRegistered(Project project, Action<SourceSet> action) {
        Set<SourceSet> processedSourceSets = newSetFromMap(new IdentityHashMap<>());
        Action<SourceSet> wrappedAction = sourceSet -> {
            if (processedSourceSets.add(sourceSet)) {
                action.execute(sourceSet);
            }
        };

        for (var handler : ALL_WHEN_TEST_SOURCE_SET_REGISTERED) {
            handler.registerAction(project, wrappedAction);
        }
    }

}
