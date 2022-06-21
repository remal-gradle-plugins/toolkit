package name.remal.gradleplugins.toolkit.classpath;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;
import lombok.val;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Unmodifiable;

interface ClasspathFileMethods {

    @Unmodifiable
    Collection<String> getResourceNames();

    @Nullable
    @MustBeClosed
    InputStream openStream(@Language("file-reference") String resourceName);

    /**
     * <p>Process each resource.</p>
     * <p>Using this method is must faster for JAR files, than using {@link #getResourceNames()} with
     * {@link #openStream(String)}.</p>
     * <p>The performance boot is gained by using {@link ZipInputStream} instead of {@link ZipFile}.</p>
     */
    void forEachResource(ResourceProcessor processor);


    @Unmodifiable
    default Collection<String> getClassNames() {
        return ImmutableList.copyOf(getResourceNames().stream()
            .filter(resourceName -> resourceName.endsWith(".class"))
            .collect(toList())
        );
    }

    @Nullable
    @MustBeClosed
    @SuppressWarnings("InjectedReferences")
    default InputStream openClassStream(String className) {
        val resourceName = className.replace('.', '/') + ".class";
        return openStream(resourceName);
    }

    @Nullable
    @MustBeClosed
    default InputStream openClassStream(Class<?> clazz) {
        return openClassStream(clazz.getName());
    }

    default void forEachClassResource(ClassProcessor processor) {
        forEachResource((resourceName, inputStreamOpener) -> {
            val className = resourceName
                .substring(0, resourceName.length() - ".class".length())
                .replace('/', '.');
            processor.process(className, inputStreamOpener);
        });
    }


    ClassesIndex getClassesIndex();


    @Unmodifiable
    Map<String, Collection<String>> getAllServices();

    @Unmodifiable
    default Collection<String> getServices(String serviceClassName) {
        val implClassNames = getAllServices().get(serviceClassName);
        return implClassNames != null ? implClassNames : emptyList();
    }

    @Unmodifiable
    default Collection<String> getServices(Class<?> serviceClass) {
        return getServices(serviceClass.getName());
    }


    @Unmodifiable
    Map<String, Collection<String>> getAllSpringFactories();

    @Unmodifiable
    default Collection<String> getSpringFactories(String factoryClassName) {
        val implClassNames = getAllSpringFactories().get(factoryClassName);
        return implClassNames != null ? implClassNames : emptyList();
    }

    @Unmodifiable
    default Collection<String> getSpringFactories(Class<?> factoryClass) {
        return getSpringFactories(factoryClass.getName());
    }

}
