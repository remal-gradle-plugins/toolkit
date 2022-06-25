package name.remal.gradleplugins.toolkit.classpath;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toCollection;
import static name.remal.gradleplugins.toolkit.PredicateUtils.not;
import static name.remal.gradleplugins.toolkit.classpath.Utils.toImmutableSet;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.getClassHierarchy;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

public final class ClassesIndex {

    @VisibleForTesting
    @SuppressWarnings({"ProtectedMemberInFinalClass", "ProtectedMembersInFinalClass"})
    protected final Map<String, Set<String>> parentClassNames = new LinkedHashMap<>();

    ClassesIndex() {
    }

    ClassesIndex(Map<String, ? extends Collection<String>> parentClassNames) {
        parentClassNames.forEach(this::registerParentClasses);
    }

    ClassesIndex(Collection<ClassesIndex> classesIndices) {
        classesIndices.forEach(classesIndex -> {
            classesIndex.parentClassNames.forEach(this::registerParentClasses);
        });
    }


    @Unmodifiable
    public Set<String> getClassNamesAssignableTo(Class<?> clazz) {
        return getClassNamesAssignableTo(clazz.getName());
    }

    @Unmodifiable
    public Set<String> getClassNamesAssignableTo(String className) {
        val builder = ImmutableSet.<String>builder();
        parentClassNames.forEach((currentClassName, currentParentClassNames) -> {
            if (currentParentClassNames.contains(className)) {
                builder.add(currentClassName);
            }
        });
        return builder.build();
    }

    @Unmodifiable
    public Set<String> getClassNamesAssignableFrom(Class<?> clazz) {
        return getClassNamesAssignableFrom(clazz.getName());
    }

    @Unmodifiable
    public Set<String> getClassNamesAssignableFrom(String className) {
        return toImmutableSet(parentClassNames.get(className));
    }


    //#region registerParentClasses()

    void registerParentClass(String className, String parentClassName) {
        registerParentClasses(className, singletonList(parentClassName));
    }

    void registerParentClasses(String className, Collection<String> parentClassNames) {
        if (parentClassNames.isEmpty()) {
            return;
        }

        Set<String> processedParentClassNames = parentClassNames.stream()
            .filter(Objects::nonNull)
            .flatMap(parentClassName -> Stream.concat(
                Stream.of(parentClassName),
                streamParentClassNamesOf(parentClassName)
            ))
            .flatMap(parentClassName -> Stream.concat(
                Stream.of(parentClassName),
                getSystemParentClasses(parentClassName).stream()
            ))
            .filter(not(className::equals))
            .collect(toCollection(LinkedHashSet::new));

        this.parentClassNames.values().forEach(currentParentClassNames -> {
            if (currentParentClassNames.contains(className)) {
                currentParentClassNames.addAll(processedParentClassNames);
            }
        });

        this.parentClassNames.put(className, processedParentClassNames);
    }

    private Stream<String> streamParentClassNamesOf(String className) {
        val classParentClassNames = parentClassNames.get(className);
        return classParentClassNames == null ? Stream.empty() : classParentClassNames.stream();
    }

    //#endregion


    //#region System classes

    private static final ConcurrentMap<String, Set<String>> SYSTEM_PARENT_CLASSES_CACHE = new ConcurrentHashMap<>();

    private static final Set<String> NOT_A_SYSTEM_CLASS = new HashSet<>(0);

    private static Set<String> getSystemParentClasses(String className) {
        return SYSTEM_PARENT_CLASSES_CACHE.computeIfAbsent(className, ClassesIndex::getSystemParentClassesImpl);
    }

    private static Set<String> getSystemParentClassesImpl(String className) {
        final Class<?> clazz;
        try {
            clazz = Class.forName(className, false, getSystemClassLoader());
        } catch (ClassNotFoundException expected) {
            return NOT_A_SYSTEM_CLASS;
        }

        val hierarchy = getClassHierarchy(clazz).stream()
            .map(Class::getName)
            .filter(not(className::equals))
            .collect(toCollection(LinkedHashSet::new));
        return toImmutableSet(hierarchy);
    }

    //#endregion

}
