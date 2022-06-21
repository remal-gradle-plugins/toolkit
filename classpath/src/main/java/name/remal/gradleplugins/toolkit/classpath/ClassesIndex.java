package name.remal.gradleplugins.toolkit.classpath;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static name.remal.gradleplugins.toolkit.classpath.Utils.toDeepImmutableSetMap;
import static name.remal.gradleplugins.toolkit.classpath.Utils.toDeepMutableSetMap;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

public final class ClassesIndex {

    @Unmodifiable
    private final Map<String, Set<String>> parentClasses;

    ClassesIndex(Map<String, Set<String>> parentClasses) {
        parentClasses = toDeepMutableSetMap(parentClasses);
        expandAssignableTo(parentClasses);
        this.parentClasses = toDeepImmutableSetMap(parentClasses);
    }

    ClassesIndex(List<ClassesIndex> classesIndices) {
        if (classesIndices.isEmpty()) {
            parentClasses = emptyMap();

        } else {
            val newParentClasses = toDeepMutableSetMap(classesIndices.get(0).parentClasses);
            for (int i = 1; i < classesIndices.size(); ++i) {
                classesIndices.get(i).parentClasses.forEach((key, values) ->
                    newParentClasses.computeIfAbsent(key, __ -> new LinkedHashSet<>())
                        .addAll(values)
                );
            }
            expandAssignableTo(newParentClasses);
            this.parentClasses = toDeepImmutableSetMap(newParentClasses);
        }
    }

    @SuppressWarnings("java:S3776")
    private static void expandAssignableTo(Map<String, Set<String>> assignableTo) {
        while (true) {
            boolean isChanged = false;
            val classNames = ImmutableList.copyOf(assignableTo.keySet());
            for (val className : classNames) {
                val parentClassNames = assignableTo.get(className);
                for (val parentClassName : ImmutableList.copyOf(parentClassNames)) {
                    val parentParentClassnames = assignableTo.get(parentClassName);
                    if (parentParentClassnames != null) {
                        isChanged |= parentClassNames.addAll(parentParentClassnames);
                        continue;
                    }

                    val systemParentClasses = getSystemParentClasses(parentClassName);
                    if (systemParentClasses != null) {
                        isChanged |= parentClassNames.addAll(systemParentClasses);
                    }
                }
            }

            if (!isChanged) {
                break;
            }
        }
    }

    @Unmodifiable
    @VisibleForTesting
    Map<String, Set<String>> getParentClasses() {
        return parentClasses;
    }


    @Unmodifiable
    public Collection<String> getClassNamesAssignableTo(Class<?> clazz) {
        return getClassNamesAssignableTo(clazz.getName());
    }

    @Unmodifiable
    public Collection<String> getClassNamesAssignableTo(String className) {
        val builder = ImmutableList.<String>builder();
        parentClasses.forEach((currentClassName, parentClassNames) -> {
            if (parentClassNames.contains(className)) {
                builder.add(currentClassName);
            }
        });
        return builder.build();
    }

    @Unmodifiable
    public Collection<String> getClassNamesAssignableFrom(Class<?> clazz) {
        return getClassNamesAssignableFrom(clazz.getName());
    }

    @Unmodifiable
    public Collection<String> getClassNamesAssignableFrom(String className) {
        val result = parentClasses.get(className);
        if (result != null) {
            return ImmutableList.copyOf(result);
        } else {
            return emptyList();
        }
    }


    //#region System classes

    @Nullable
    private static LinkedHashSet<String> getSystemParentClasses(String className) {
        final Class<?> clazz;
        try {
            clazz = Class.forName(className, false, getSystemClassLoader());
        } catch (ClassNotFoundException expected) {
            return null;
        }

        LinkedHashSet<String> result = new LinkedHashSet<>();
        Stream.concat(
                Stream.of(clazz.getSuperclass()),
                stream(clazz.getInterfaces())
            )
            .filter(Objects::nonNull)
            .map(Class::getName)
            .forEach(result::add);
        return result;
    }

    //#endregion

}
