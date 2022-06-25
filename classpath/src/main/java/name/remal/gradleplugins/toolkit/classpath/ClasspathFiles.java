package name.remal.gradleplugins.toolkit.classpath;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static name.remal.gradleplugins.toolkit.classpath.Utils.toDeepImmutableSetMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradleplugins.toolkit.LazyInitializer;
import org.gradle.api.JavaVersion;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Unmodifiable;

public final class ClasspathFiles implements ClasspathFileMethods {

    private final List<ClasspathFileBase> files;

    public ClasspathFiles() {
        this.files = emptyList();
    }

    public ClasspathFiles(Iterable<? extends File> files) {
        this(files, JavaVersion.current());
    }

    public ClasspathFiles(JavaVersion jvmCompatibilityVersion) {
        this(emptyList(), jvmCompatibilityVersion);
    }

    public ClasspathFiles(
        Iterable<? extends File> files,
        JavaVersion compatibilityVersion
    ) {
        val jvmMajorCompatibilityVersion = parseInt(compatibilityVersion.getMajorVersion());
        val filesBuilder = ImmutableList.<ClasspathFileBase>builder();
        StreamSupport.stream(files.spliterator(), false)
            .filter(Objects::nonNull)
            .distinct()
            .map(file -> ClasspathFileBase.of(file, jvmMajorCompatibilityVersion))
            .forEach(filesBuilder::add);
        this.files = filesBuilder.build();
    }


    private ClasspathFiles(List<ClasspathFileBase> files) {
        this.files = ImmutableList.copyOf(files);
    }

    public ClasspathFiles plus(ClasspathFiles other) {
        return new ClasspathFiles(
            Stream.of(
                    this.files,
                    other.files
                )
                .flatMap(Collection::stream)
                .distinct()
                .collect(toList())
        );
    }


    @Override
    @Unmodifiable
    public Set<String> getResourceNames() {
        val builder = ImmutableSet.<String>builder();
        files.stream()
            .map(ClasspathFileMethods::getResourceNames)
            .flatMap(Collection::stream)
            .distinct()
            .sorted()
            .forEach(builder::add);
        return builder.build();
    }

    @Override
    @Nullable
    @MustBeClosed
    @SuppressWarnings("MustBeClosedChecker")
    public InputStream openStream(@Language("file-reference") String resourceName) {
        for (val file : files) {
            val inputStream = file.openStream(resourceName);
            if (inputStream != null) {
                return inputStream;
            }
        }
        return null;
    }

    @Override
    public void forEachResource(ResourceProcessor processor) {
        for (val file : files) {
            file.forEachResource(processor);
        }
    }

    @Override
    public ClassesIndex getClassesIndex() {
        return classesIndex.get();
    }

    private final LazyInitializer<ClassesIndex> classesIndex = new LazyInitializer<ClassesIndex>() {
        @Override
        protected ClassesIndex create() {
            val classIndexes = files.stream()
                .map(ClasspathFileMethods::getClassesIndex)
                .collect(toList());
            return new ClassesIndex(classIndexes);
        }
    };

    @Override
    @Unmodifiable
    public Map<String, Set<String>> getAllServices() {
        return getAllServicesImpl(ClasspathFileMethods::getAllServices);
    }

    @Override
    @Unmodifiable
    public Map<String, Set<String>> getAllSpringFactories() {
        return getAllServicesImpl(ClasspathFileMethods::getAllSpringFactories);
    }

    private Map<String, Set<String>> getAllServicesImpl(
        Function<ClasspathFileMethods, Map<String, Set<String>>> getter
    ) {
        Map<String, Set<String>> allServices = new LinkedHashMap<>();
        for (val file : files) {
            val allFileServices = getter.apply(file);
            allFileServices.forEach((serviceClassName, implClassNames) -> {
                val allImplClassNames = allServices.computeIfAbsent(serviceClassName, __ -> new LinkedHashSet<>());
                allImplClassNames.addAll(implClassNames);
            });
        }
        return toDeepImmutableSetMap(allServices);
    }

}
