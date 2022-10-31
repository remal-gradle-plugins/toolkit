package name.remal.gradleplugins.toolkit;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.AbstractCompileUtils.getDestinationDir;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradleplugins.toolkit.reflection.MethodsInvoker.invokeMethod;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.isGetterOf;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.tryLoadClass;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

@NoArgsConstructor(access = PRIVATE)
public abstract class SourceSetUtils {

    private static final Pattern GET_CONFIGURATION_NAME_METHOD_NAME = Pattern.compile(
        "^get[A-Z].*[a-z]ConfigurationName$"
    );

    private static final List<Method> GET_CONFIGURATION_NAME_METHODS = ImmutableList.copyOf(
        Stream.of(SourceSet.class.getMethods())
            .filter(it -> GET_CONFIGURATION_NAME_METHOD_NAME.matcher(it.getName()).matches())
            .sorted(comparing(Method::getName))
            .collect(toList())
    );

    @SneakyThrows
    public static Set<String> getSourceSetConfigurationNames(SourceSet sourceSet) {
        Set<String> configurationNames = new LinkedHashSet<>();
        for (val method : GET_CONFIGURATION_NAME_METHODS) {
            val nameObject = method.invoke(sourceSet);
            if (nameObject != null) {
                val name = nameObject.toString();
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
        val configurationName = configuration.getName();
        return isSourceSetConfigurationName(sourceSet, configurationName);
    }


    public static boolean isProcessedBy(SourceSet sourceSet, SourceTask task) {
        val result = new AtomicBoolean(false);
        val allSource = sourceSet.getAllSource();
        task.getSource().visit(details -> {
            if (isNotArchiveEntry(details)) {
                val file = details.getFile();
                if (allSource.contains(file)) {
                    result.set(true);
                    details.stopVisiting();
                }
            }
        });
        return result.get();
    }

    public static boolean isProcessedBy(SourceSet sourceSet, AbstractCopyTask task) {
        val result = new AtomicBoolean(false);
        val allSource = sourceSet.getAllSource();
        task.getSource().getAsFileTree().visit(details -> {
            if (isNotArchiveEntry(details)) {
                val file = details.getFile();
                if (allSource.contains(file)) {
                    result.set(true);
                    details.stopVisiting();
                }
            }
        });
        return result.get();
    }

    @ReliesOnInternalGradleApi
    @VisibleForTesting
    static final Set<Class<?>> ABSTRACT_ARCHIVE_FILE_TREE_CLASSES = Stream.of(
            tryLoadClass("org.gradle.api.internal.file.archive.AbstractArchiveFileTree"),
            tryLoadClass("org.gradle.api.internal.file.collections.FileSystemMirroringFileTree")
        )
        .filter(Objects::nonNull)
        .collect(toCollection(LinkedHashSet::new));

    private static final Set<String> ARCHIVE_FILE_TREE_SIMPLE_CLASS_NAMES = unmodifiableSet(new LinkedHashSet<>(asList(
        // supported by Gradle natively:
        "TarFileTree",
        "ZipFileTree",

        // supported by https://github.com/freefair/gradle-plugins/:
        "ArFileTree",
        "ArchiveFileTree",
        "ArjFileTree",
        "DumpFileTree",
        "SevenZipFileTree"
    )));

    @VisibleForTesting
    static boolean isNotArchiveEntry(FileTreeElement details) {
        val detailsClass = unwrapGeneratedSubclass(details.getClass());
        val enclosingClass = detailsClass.getEnclosingClass();
        if (enclosingClass == null) {
            return true;
        }
        val isArchive = ABSTRACT_ARCHIVE_FILE_TREE_CLASSES.contains(enclosingClass)
            || ARCHIVE_FILE_TREE_SIMPLE_CLASS_NAMES.contains(enclosingClass.getSimpleName());
        return !isArchive;
    }

    public static boolean isCompiledBy(SourceSet sourceSet, AbstractCompile task) {
        val destinationDir = getDestinationDir(task);
        if (destinationDir == null) {
            return isProcessedBy(sourceSet, task);
        }

        return sourceSet.getOutput().getClassesDirs().contains(destinationDir);
    }

    private static final Pattern GET_TASK_NAME_METHOD_NAME = Pattern.compile(
        "^get[A-Z].*[a-z]TaskName$"
    );

    private static final List<Method> GET_TASK_NAME_METHODS = Stream.of(SourceSet.class.getMethods())
        .filter(method -> isGetterOf(method, String.class))
        .filter(it -> GET_TASK_NAME_METHOD_NAME.matcher(it.getName()).matches())
        .sorted(comparing(Method::getName))
        .collect(toList());

    @SneakyThrows
    public static boolean isSourceSetTask(SourceSet sourceSet, Task task) {
        for (val method : GET_TASK_NAME_METHODS) {
            val nameObject = method.invoke(sourceSet);
            if (nameObject != null) {
                val name = nameObject.toString();
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
        .filter(method -> isGetterOf(method, SourceDirectorySet.class))
        .sorted(comparing(Method::getName))
        .collect(toList());

    @Unmodifiable
    @ReliesOnInternalGradleApi
    @SneakyThrows
    @SuppressWarnings("deprecation")
    public static Collection<SourceDirectorySet> getAllSourceDirectorySets(SourceSet sourceSet) {
        Collection<SourceDirectorySet> result = newSetFromMap(new IdentityHashMap<>());

        for (val method : GET_SOURCE_DIRECTORY_SET_METHODS) {
            val sourceDirectorySet = (SourceDirectorySet) method.invoke(sourceSet);
            result.add(sourceDirectorySet);
        }

        if (sourceSet instanceof org.gradle.api.internal.HasConvention) {
            val convention = ((org.gradle.api.internal.HasConvention) sourceSet).getConvention();
            for (val pluginEntry : convention.getPlugins().entrySet()) {
                val plugin = pluginEntry.getValue();
                for (val pluginMethod : plugin.getClass().getMethods()) {
                    if (isGetterOf(pluginMethod, SourceDirectorySet.class)) {
                        val sourceDirectorySet = (SourceDirectorySet) pluginMethod.invoke(plugin);
                        result.add(sourceDirectorySet);
                    }
                }
            }
        }

        return unmodifiableCollection(result);
    }


    public static void whenTestSourceSetRegistered(Project project, Action<SourceSet> action) {
        Set<SourceSet> processedSourceSets = newSetFromMap(new IdentityHashMap<>());
        Action<SourceSet> wrappedAction = sourceSet -> {
            if (processedSourceSets.add(sourceSet)) {
                action.execute(sourceSet);
            }
        };

        project.getPluginManager().withPlugin("java", __ -> {
            val sourceSets = getExtension(project, SourceSetContainer.class);
            val testSourceSet = sourceSets.getByName(TEST_SOURCE_SET_NAME);
            wrappedAction.execute(testSourceSet);

            sourceSets
                .matching(sourceSet -> {
                    val normalizedName = LOWER_CAMEL.to(LOWER_HYPHEN, sourceSet.getName());
                    return normalizedName.endsWith("-test")
                        || normalizedName.endsWith("-tests")
                        || normalizedName.endsWith("_test")
                        || normalizedName.endsWith("_tests")
                        ;
                })
                .all(wrappedAction);
        });

        project.getPluginManager().withPlugin("jvm-test-suite", __ -> {
            val testing = getExtension(project, "testing");
            val untypedSuites = invokeMethod(testing, DomainObjectCollection.class, "getSuites");
            @SuppressWarnings("unchecked") val suites = (NamedDomainObjectContainer<Object>) untypedSuites;
            suites.all(suite -> {
                @SuppressWarnings("unchecked")
                val getSourcesMethod = findMethod((Class<Object>) suite.getClass(), SourceSet.class, "getSources");
                if (getSourcesMethod != null) {
                    val testSourceSet = getSourcesMethod.invoke(suite);
                    wrappedAction.execute(testSourceSet);
                }
            });
        });

        project.getPluginManager().withPlugin("java-test-fixtures", __ -> {
            val sourceSets = getExtension(project, SourceSetContainer.class);
            val testFixturesSourceSet = sourceSets.getByName("testFixtures");
            wrappedAction.execute(testFixturesSourceSet);
        });

        project.getPluginManager().withPlugin("name.remal.test-source-sets", __ -> {
            val testSourceSetsExtension = getExtension(project, "testSourceSets");
            @SuppressWarnings("unchecked")
            val testSourceSetsContainer = (NamedDomainObjectContainer<Object>) testSourceSetsExtension;
            testSourceSetsContainer.withType(SourceSet.class).all(wrappedAction);
        });

        project.getPluginManager().withPlugin("org.unbroken-dome.test-sets", __ -> {
            val testSetsExtension = getExtension(project, "testSets");
            @SuppressWarnings("unchecked")
            val testSets = (NamedDomainObjectContainer<Object>) testSetsExtension;
            testSets.all(testSet -> {
                val testSourceSet = invokeMethod(testSet, SourceSet.class, "getSourceSet");
                wrappedAction.execute(testSourceSet);
            });
        });
    }

}
