package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.annotations.ConfigurationPhaseOnly;
import name.remal.gradle_plugins.toolkit.annotations.DynamicCompatibilityCandidate;
import name.remal.gradle_plugins.toolkit.reflection.TypedMethod0;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
@DynamicCompatibilityCandidate
public abstract class KotlinPluginUtils {

    @Nullable
    @ConfigurationPhaseOnly
    @SneakyThrows
    public static FileCollection getKotlinCompileSources(Task task) {
        var taskClass = unwrapGeneratedSubclass(task.getClass());
        if (taskClass.getName().startsWith("org.gradle.")) {
            return null;
        }

        if (task instanceof AbstractCompile
            && taskClass.getName().startsWith("org.jetbrains.kotlin.")
        ) {
            // Kotlin <=1.6.*
            return ((AbstractCompile) task).getSource();
        }

        var taskClassLoader = taskClass.getClassLoader();
        var getSources = GET_SOURCES_CACHE.get(taskClassLoader).orElse(null);
        if (getSources != null
            && getSources.getReflectionMethod().getDeclaringClass().isInstance(task)
        ) {
            return getSources.invoke(task);
        }

        return null;
    }

    @SuppressWarnings({"unchecked", "checkstyle:LineLength"})
    private static final LoadingCache<ClassLoader, Optional<TypedMethod0<Object, FileCollection>>> GET_SOURCES_CACHE =
        CacheBuilder.newBuilder().weakKeys().build(CacheLoader.from(classLoader -> {
            final Class<?> baseClass;
            try {
                baseClass = Class.forName(
                    "org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool",
                    false,
                    classLoader
                );
            } catch (ClassNotFoundException e) {
                return Optional.empty();
            }

            var destinationDirectoryMethod = findMethod(
                (Class<Object>) baseClass,
                FileCollection.class,
                "getSources"
            );
            return Optional.ofNullable(destinationDirectoryMethod);
        }));


    @Nullable
    @ConfigurationPhaseOnly
    @SneakyThrows
    public static DirectoryProperty getKotlinCompileDestinationDirectory(Task task) {
        var taskClass = unwrapGeneratedSubclass(task.getClass());
        if (taskClass.getName().startsWith("org.gradle.")) {
            return null;
        }

        if (task instanceof AbstractCompile
            && taskClass.getName().startsWith("org.jetbrains.kotlin.")
        ) {
            // Kotlin <=1.6.*
            return ((AbstractCompile) task).getDestinationDirectory();
        }

        var taskClassLoader = taskClass.getClassLoader();
        var getDestinationDirectory = GET_DESTINATION_DIRECTORY_CACHE.get(taskClassLoader).orElse(null);
        if (getDestinationDirectory != null
            && getDestinationDirectory.getReflectionMethod().getDeclaringClass().isInstance(task)
        ) {
            return getDestinationDirectory.invoke(task);
        }

        return null;
    }

    @SuppressWarnings({"unchecked", "checkstyle:LineLength"})
    private static final LoadingCache<ClassLoader, Optional<TypedMethod0<Object, DirectoryProperty>>> GET_DESTINATION_DIRECTORY_CACHE =
        CacheBuilder.newBuilder().weakKeys().build(CacheLoader.from(classLoader -> {
            final Class<?> baseClass;
            try {
                baseClass = Class.forName(
                    "org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool",
                    false,
                    classLoader
                );
            } catch (ClassNotFoundException e) {
                return Optional.empty();
            }

            var destinationDirectoryMethod = findMethod(
                (Class<Object>) baseClass,
                DirectoryProperty.class,
                "getDestinationDirectory"
            );
            return Optional.ofNullable(destinationDirectoryMethod);
        }));

}
