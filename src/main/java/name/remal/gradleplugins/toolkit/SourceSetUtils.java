package name.remal.gradleplugins.toolkit;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.AbstractCompileUtils.getDestinationDir;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.tryLoadClass;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import java.lang.reflect.Method;
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
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.jetbrains.annotations.VisibleForTesting;

@NoArgsConstructor(access = PRIVATE)
public abstract class SourceSetUtils {

    private static final Pattern GET_CONFIGURATION_NAME_METHOD_NAME = Pattern.compile(
        "^get[A-Z].*[a-z]ConfigurationName$"
    );

    private static final List<Method> GET_CONFIGURATION_NAME_METHODS = Stream.of(SourceSet.class.getMethods())
        .filter(it -> GET_CONFIGURATION_NAME_METHOD_NAME.matcher(it.getName()).matches())
        .sorted(comparing(Method::getName))
        .collect(toList());

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

}
