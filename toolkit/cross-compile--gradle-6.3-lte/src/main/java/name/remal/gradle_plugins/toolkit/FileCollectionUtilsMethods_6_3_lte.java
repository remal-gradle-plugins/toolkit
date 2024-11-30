package name.remal.gradle_plugins.toolkit;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.CompositeFileCollection;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.internal.file.FileCollectionStructureVisitor;
import org.gradle.api.tasks.SourceSetOutput;
import org.jetbrains.annotations.Unmodifiable;

@ReliesOnInternalGradleApi
@AutoService(FileCollectionUtilsMethods.class)
final class FileCollectionUtilsMethods_6_3_lte implements FileCollectionUtilsMethods {

    @Unmodifiable
    @SuppressWarnings("java:S3776")
    public Set<Configuration> getConfigurationsUsedIn(FileCollection rootFileCollection) {
        if (rootFileCollection instanceof Configuration) {
            return singleton((Configuration) rootFileCollection);
        }


        val seenConfigurations = new LinkedHashSet<Configuration>();

        Deque<FileCollection> queue = new ArrayDeque<>();
        queue.addLast(rootFileCollection);

        val seenFileCollections = newSetFromMap(new IdentityHashMap<>());
        seenFileCollections.addAll(queue);
        while (true) {
            val fileCollection = queue.pollFirst();
            if (fileCollection == null) {
                break;
            }

            if (fileCollection instanceof Configuration) {
                seenConfigurations.add((Configuration) fileCollection);
                continue;
            }

            if (fileCollection instanceof FileTree
                || fileCollection instanceof SourceSetOutput
            ) {
                continue;
            }

            for (val sourceFileCollection : getSourceFileCollections(fileCollection)) {
                if (seenFileCollections.add(sourceFileCollection)) {
                    queue.addLast(sourceFileCollection);
                }
            }
        }

        return unmodifiableSet(seenConfigurations);
    }


    private static final Collection<String> GET_SOURCE_METHOD_NAMES = unmodifiableCollection(asList(
        "getLeft", // SubtractingFileCollection
        "getSource", // UnionFileCollection
        "getSourceCollections" // CompositeFileCollection
    ));

    /**
     * In Gradle <=6.3, implementations of
     * {@link FileCollectionInternal#visitStructure(FileCollectionStructureVisitor)} are not recursive.
     *
     * <p>This method helps to emulate {@link FileCollectionInternal#visitStructure(FileCollectionStructureVisitor)} of
     * {@link CompositeFileCollection}.
     */
    @Unmodifiable
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static Collection<FileCollection> getSourceFileCollections(FileCollection fileCollection) {
        val sourceMethod = getSourceMethod(fileCollection.getClass());
        if (sourceMethod == null) {
            return emptyList();
        }

        val currentSourceUntyped = sourceMethod.invoke(fileCollection);
        if (currentSourceUntyped == null) {
            return emptyList();
        }

        Collection<FileCollection> sourceFileCollections = newSetFromMap(new IdentityHashMap<>());
        val currentSources = currentSourceUntyped instanceof Iterable
            ? (Iterable<Object>) currentSourceUntyped
            : singletonList(currentSourceUntyped);
        for (val source : currentSources) {
            if (source instanceof FileCollection) {
                sourceFileCollections.add((FileCollection) source);
            }
        }
        return unmodifiableCollection(sourceFileCollections);
    }

    @Nullable
    @SuppressWarnings({"deprecation", "java:S3011", "java:S3776", "RedundantSuppression"})
    private static synchronized Method getSourceMethod(Class<?> fileCollectionClass) {
        if (!FileCollection.class.isAssignableFrom(fileCollectionClass)) {
            return null;
        }

        return GET_SOURCE_METHODS.computeIfAbsent(fileCollectionClass, clazz -> {
            for (val methodName : GET_SOURCE_METHOD_NAMES) {
                Class<?> currentClass = clazz;
                while (currentClass != null && currentClass != Object.class) {
                    val getSourceMethod = stream(currentClass.getDeclaredMethods())
                        .filter(method -> !(
                            method.isSynthetic()
                                || isAbstract(method.getModifiers())
                                || isStatic(method.getModifiers())
                                || isPrivate(method.getModifiers())
                        ))
                        .filter(method ->
                            method.getParameterCount() == 0
                                && method.getName().equals(methodName)
                        )
                        .findFirst()
                        .orElse(null);
                    if (getSourceMethod == null) {
                        currentClass = currentClass.getSuperclass();
                        continue;
                    }

                    if (!getSourceMethod.isAccessible()) {
                        if (!isPublic(getSourceMethod.getModifiers())
                            || !isPublic(getSourceMethod.getDeclaringClass().getModifiers())
                        ) {
                            getSourceMethod.setAccessible(true);
                        }
                    }

                    return getSourceMethod;
                }
            }

            return null;
        });
    }

    private static final Map<Class<?>, Method> GET_SOURCE_METHODS = new WeakHashMap<>();

}
