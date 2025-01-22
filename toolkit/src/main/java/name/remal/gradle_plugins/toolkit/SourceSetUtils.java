package name.remal.gradle_plugins.toolkit;

import static java.util.Arrays.stream;
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadAllCrossCompileServiceImplementations;
import static name.remal.gradle_plugins.toolkit.FileTreeElementUtils.isNotArchiveEntry;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyListProxy;
import static name.remal.gradle_plugins.toolkit.ThrowableUtils.unwrapReflectionException;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isGetterOf;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isNotStatic;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
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

    public static boolean isCompiledBy(SourceSet sourceSet, AbstractCompile task) {
        var destinationDir = task.getDestinationDirectory().getAsFile().getOrNull();
        if (destinationDir == null) {
            return isProcessedBy(sourceSet, task);
        }

        return sourceSet.getOutput().getClassesDirs().contains(destinationDir);
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

        if (task instanceof AbstractCompile) {
            return isCompiledBy(sourceSet, (AbstractCompile) task);
        }
        if (task instanceof SourceTask) {
            return isProcessedBy(sourceSet, (SourceTask) task);
        }
        if (task instanceof AbstractCopyTask) {
            return isProcessedBy(sourceSet, (AbstractCopyTask) task);
        }

        return false;
    }


    private static final List<Method> GET_SOURCE_DIRECTORY_SET_METHODS = stream(SourceSet.class.getMethods())
        .filter(ReflectionUtils::isNotStatic)
        .filter(method -> isGetterOf(method, SourceDirectorySet.class))
        .sorted(comparing(Method::getName))
        .collect(toUnmodifiableList());

    @Unmodifiable
    @ReliesOnInternalGradleApi
    @SneakyThrows
    @SuppressWarnings({"deprecation", "java:S3776"})
    public static Collection<SourceDirectorySet> getAllSourceDirectorySets(SourceSet sourceSet) {
        Collection<SourceDirectorySet> result = newSetFromMap(new IdentityHashMap<>());

        for (var method : GET_SOURCE_DIRECTORY_SET_METHODS) {
            final SourceDirectorySet sourceDirectorySet;
            try {
                sourceDirectorySet = (SourceDirectorySet) method.invoke(sourceSet);
            } catch (Throwable e) {
                throw unwrapReflectionException(e);
            }

            result.add(sourceDirectorySet);
        }

        if (sourceSet instanceof org.gradle.api.internal.HasConvention) {
            var convention = ((org.gradle.api.internal.HasConvention) sourceSet).getConvention();
            for (var pluginEntry : convention.getPlugins().entrySet()) {
                var plugin = pluginEntry.getValue();
                for (var pluginMethod : plugin.getClass().getMethods()) {
                    if (isNotStatic(pluginMethod) && isGetterOf(pluginMethod, SourceDirectorySet.class)) {
                        final SourceDirectorySet sourceDirectorySet;
                        try {
                            sourceDirectorySet = (SourceDirectorySet) pluginMethod.invoke(plugin);
                        } catch (Throwable e) {
                            throw unwrapReflectionException(e);
                        }

                        result.add(sourceDirectorySet);
                    }
                }
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
