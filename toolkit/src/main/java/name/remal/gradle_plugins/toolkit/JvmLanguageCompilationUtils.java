package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.getKotlinCompileDestinationDirectory;
import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.getKotlinCompileSources;
import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.getKotlinLibraries;
import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.setKotlinLibraries;
import static name.remal.gradle_plugins.toolkit.SneakyThrowUtils.sneakyThrowsAction;
import static org.gradle.api.specs.Specs.satisfyAll;

import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.ConfigurationPhaseOnly;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class JvmLanguageCompilationUtils {

    public static boolean isJvmLanguageCompileTask(Task task) {
        return getJvmLanguagesCompileTaskProperties(task) != null;
    }


    @Nullable
    public static JvmLanguagesCompileTaskProperties getJvmLanguagesCompileTaskProperties(Task task) {
        if (task instanceof AbstractCompile) {
            var compileTask = (AbstractCompile) task;
            return new JvmLanguagesCompileTaskProperties() {
                @Override
                public FileTree getSource() {
                    return compileTask.getSource();
                }

                @Override
                public DirectoryProperty getDestinationDirectory() {
                    return compileTask.getDestinationDirectory();
                }

                @Override
                public FileCollection getClasspath() {
                    return compileTask.getClasspath();
                }

                @Override
                public void setClasspath(FileCollection classpath) {
                    compileTask.setClasspath(classpath);
                }

                @Override
                public String toString() {
                    return "JVM compilation properties of task " + compileTask;
                }
            };
        }

        var kotlinCompileSources = getKotlinCompileSources(task);
        if (kotlinCompileSources != null) {
            var kotlinCompileDestinationDirectory = getKotlinCompileDestinationDirectory(task);
            if (kotlinCompileDestinationDirectory != null) {
                var kotlinCompileSourcesTree = kotlinCompileSources.getAsFileTree();
                return new JvmLanguagesCompileTaskProperties() {
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
                        return "JVM compilation properties of task " + task;
                    }
                };
            }
        }

        return null; // not a known JVM language compilation task
    }

    public interface JvmLanguagesCompileTaskProperties {

        FileTree getSource();

        DirectoryProperty getDestinationDirectory();

        FileCollection getClasspath();

        void setClasspath(FileCollection classpath);

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
        tasks.matching(spec).configureEach(sneakyThrowsAction(task -> {
            var properties = getJvmLanguagesCompileTaskProperties(task);
            if (properties != null) {
                configurer.configure(task, properties);
            }
        }));
    }

    @FunctionalInterface
    public interface JvmLanguagesCompileTaskConfigurer {
        void configure(Task task, JvmLanguagesCompileTaskProperties properties) throws Throwable;
    }

}
