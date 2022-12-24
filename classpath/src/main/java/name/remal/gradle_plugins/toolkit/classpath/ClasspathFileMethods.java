package name.remal.gradle_plugins.toolkit.classpath;

import static java.util.stream.Collectors.toCollection;
import static name.remal.gradle_plugins.toolkit.classpath.Utils.toImmutableSet;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;
import lombok.val;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Unmodifiable;

interface ClasspathFileMethods {

    @Unmodifiable
    Set<String> getResourceNames();

    default boolean hasResource(@Language("file-reference") String resourceName) {
        return getResourceNames().contains(resourceName);
    }

    /**
     * Opens {@link InputStream} for the resource.
     *
     * @return {@code null} if the resource can't be found
     */
    @Nullable
    @MustBeClosed
    InputStream openStream(@Language("file-reference") String resourceName);

    /**
     * <p>Process each resource.</p>
     * <p>Using this method is must faster for JAR files, than using {@link #getResourceNames()} with
     * {@link #openStream(String)}.</p>
     * <p>The performance boot is gained by using {@link ZipInputStream} instead of
     * {@link ZipFile#getInputStream(ZipEntry)}.</p>
     */
    void forEachResource(ResourceProcessor processor);

    /**
     * <p>Find all resource with specific resource name (handle duplicates) and process them.</p>
     */
    void forEachResource(@Language("file-reference") String resourceName, ResourceProcessor processor);


    @Unmodifiable
    default Set<String> getClassNames() {
        val classResourceSuffix = ".class";
        return toImmutableSet(getResourceNames().stream()
            .filter(resourceName -> resourceName.endsWith(classResourceSuffix))
            .map(resourceName -> resourceName.substring(0, resourceName.length() - classResourceSuffix.length()))
            .map(resourceName -> resourceName.replace('/', '.'))
            .collect(toCollection(LinkedHashSet::new))
        );
    }

    @SuppressWarnings("InjectedReferences")
    default boolean hasClass(String className) {
        val resourceName = className.replace('.', '/') + ".class";
        return hasResource(resourceName);
    }

    default boolean hasClass(Class<?> clazz) {
        return hasClass(clazz.getName());
    }

    /**
     * Opens {@link InputStream} for the class resource by the class name.
     *
     * @return {@code null} if the class resource can't be found
     */
    @Nullable
    @MustBeClosed
    @SuppressWarnings("InjectedReferences")
    default InputStream openClassStream(String className) {
        val resourceName = className.replace('.', '/') + ".class";
        return openStream(resourceName);
    }

    /**
     * @see #openClassStream(String)
     */
    @Nullable
    @MustBeClosed
    default InputStream openClassStream(Class<?> clazz) {
        return openClassStream(clazz.getName());
    }

    /**
     * <p>Process each class resource.</p>
     * <p>Using this method is must faster for JAR files, than using {@link #getResourceNames()} with
     * {@link #openClassStream(String)}.</p>
     * <p>The performance boot is gained by using {@link ZipInputStream} instead of
     * {@link ZipFile#getInputStream(ZipEntry)}.</p>
     */
    default void forEachClassResource(ClassProcessor processor) {
        forEachResource((classpathFile, resourceName, inputStreamOpener) -> {
            if (!resourceName.endsWith(".class")) {
                return;
            }

            val className = resourceName
                .substring(0, resourceName.length() - ".class".length())
                .replace('/', '.');
            processor.process(classpathFile, className, inputStreamOpener);
        });
    }


    ClassesIndex getClassesIndex();


    @Unmodifiable
    Map<String, Set<String>> getAllServices();

    @Unmodifiable
    default Set<String> getServices(String serviceClassName) {
        return toImmutableSet(getAllServices().get(serviceClassName));
    }

    @Unmodifiable
    default Set<String> getServices(Class<?> serviceClass) {
        return getServices(serviceClass.getName());
    }


    @Unmodifiable
    Map<String, Set<String>> getAllSpringFactories();

    @Unmodifiable
    default Set<String> getSpringFactories(String factoryClassName) {
        return toImmutableSet(getAllSpringFactories().get(factoryClassName));
    }

    @Unmodifiable
    default Set<String> getSpringFactories(Class<?> factoryClass) {
        return getSpringFactories(factoryClass.getName());
    }

}
