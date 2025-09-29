package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.getKotlinCompileDestinationDirectory;
import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.getKotlinCompileSources;
import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.getKotlinLibraries;
import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.setKotlinLibraries;
import static name.remal.gradle_plugins.toolkit.SneakyThrowUtils.sneakyThrowsAction;
import static org.gradle.api.specs.Specs.satisfyAll;

import java.util.LinkedHashSet;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.ConfigurationPhaseOnly;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.AbstractCompile;

@NoArgsConstructor(access = PRIVATE)
public abstract class JvmLanguageCompilationUtils {

    public static boolean isJvmLanguageCompileTask(Task task) {
        if (task instanceof AbstractCompile) {
            return true;
        }

        var kotlinCompileSources = getKotlinCompileSources(task);
        if (kotlinCompileSources != null) {
            var kotlinCompileDestinationDirectory = getKotlinCompileDestinationDirectory(task);
            if (kotlinCompileDestinationDirectory != null) {
                return true;
            }
        }

        return false;
    }


    @ConfigurationPhaseOnly
    public static void configureJvmLanguageCompileTasks(
        TaskContainer tasks,
        JvmLanguagesCompileTaskConfigurer configurer
    ) {
        configureJvmLanguageCompileTasks(tasks, satisfyAll(), configurer);
    }

    @ConfigurationPhaseOnly
    public static void configureJvmLanguageCompileTasks(
        TaskContainer tasks,
        Spec<? super Task> spec,
        JvmLanguagesCompileTaskConfigurer configurer
    ) {
        var configuredTasks = new LinkedHashSet<Task>();

        tasks.withType(AbstractCompile.class)
            .matching(spec)
            .matching(configuredTasks::add)
            .configureEach(sneakyThrowsAction(task -> {
                var properties = new JvmLanguagesCompileTaskProperties() {
                    @Override
                    public FileTree getSource() {
                        return task.getSource();
                    }

                    @Override
                    public DirectoryProperty getDestinationDirectory() {
                        return task.getDestinationDirectory();
                    }

                    @Override
                    public FileCollection getClasspath() {
                        return task.getClasspath();
                    }

                    @Override
                    public void setClasspath(FileCollection classpath) {
                        task.setClasspath(classpath);
                    }

                    @Override
                    public String toString() {
                        return "Properties of task " + task;
                    }
                };
                configurer.configure(task, properties);
            }));

        tasks
            .matching(spec)
            .configureEach(sneakyThrowsAction(task -> {
                if (configuredTasks.contains(task)) {
                    return;
                }

                var kotlinCompileSources = getKotlinCompileSources(task);
                if (kotlinCompileSources == null) {
                    return; // not a Kotlin compilation task
                }

                var kotlinCompileDestinationDirectory = getKotlinCompileDestinationDirectory(task);
                if (kotlinCompileDestinationDirectory == null) {
                    return; // not a Kotlin compilation task
                }

                if (!configuredTasks.add(task)) {
                    return;
                }

                var kotlinCompileSourcesTree = kotlinCompileSources.getAsFileTree();
                var properties = new JvmLanguagesCompileTaskProperties() {
                    @Override
                    public FileTree getSource() {
                        return kotlinCompileSourcesTree;
                    }

                    @Override
                    public DirectoryProperty getDestinationDirectory() {
                        return kotlinCompileDestinationDirectory;
                    }

                    @Override
                    public FileCollection getClasspath() {
                        var libraries = getKotlinLibraries(task);
                        if (libraries == null) {
                            throw new UnsupportedOperationException("Can't get Kotlin libraries of task " + task);
                        }
                        return libraries;
                    }

                    @Override
                    public void setClasspath(FileCollection classpath) {
                        if (!setKotlinLibraries(task, classpath)) {
                            throw new UnsupportedOperationException("Can't set Kotlin libraries for task " + task);
                        }
                    }

                    @Override
                    public String toString() {
                        return "Properties of task " + task;
                    }
                };
                configurer.configure(task, properties);
            }));
    }

    @FunctionalInterface
    public interface JvmLanguagesCompileTaskConfigurer {
        void configure(Task task, JvmLanguagesCompileTaskProperties properties) throws Throwable;
    }

    public interface JvmLanguagesCompileTaskProperties {

        FileTree getSource();

        DirectoryProperty getDestinationDirectory();

        FileCollection getClasspath();

        void setClasspath(FileCollection classpath);

    }

}
