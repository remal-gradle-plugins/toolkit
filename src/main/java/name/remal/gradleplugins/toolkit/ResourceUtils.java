package name.remal.gradleplugins.toolkit;

import static java.lang.String.format;
import static name.remal.gradleplugins.toolkit.reflection.WhoCalled.getCallingClass;

import java.io.InputStream;
import java.net.URL;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import org.intellij.lang.annotations.Language;

public interface ResourceUtils {

    @Nullable
    static URL findResourceUrl(@Language("file-reference") String name, Class<?> loadingClass) {
        return loadingClass.getResource(name);
    }

    @Nullable
    static URL findResourceUrl(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        val trueClassLoader = classLoader != null ? classLoader : ClassLoader.getSystemClassLoader();
        return trueClassLoader.getResource(name);
    }

    @Nullable
    @SuppressWarnings("java:S109")
    static URL findResourceUrl(@Language("file-reference") String name) {
        val callingClass = getCallingClass(2);
        return findResourceUrl(name, callingClass.getClassLoader());
    }


    static URL getResourceUrl(@Language("file-reference") String name, Class<?> loadingClass) {
        val url = findResourceUrl(name, loadingClass);
        if (url == null) {
            throw new IllegalStateException(format(
                "Resource can't be found for %s: %s",
                loadingClass,
                name
            ));
        }
        return url;
    }

    static URL getResourceUrl(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        val url = findResourceUrl(name, classLoader);
        if (url == null) {
            throw new IllegalStateException(format(
                "Resource can't be found: %s",
                name
            ));
        }
        return url;
    }

    @SuppressWarnings("java:S109")
    static URL getResourceUrl(@Language("file-reference") String name) {
        val callingClass = getCallingClass(2);
        return getResourceUrl(name, callingClass.getClassLoader());
    }


    @SneakyThrows
    static InputStream openResource(@Language("file-reference") String name, Class<?> loadingClass) {
        return getResourceUrl(name, loadingClass).openStream();
    }

    @SneakyThrows
    static InputStream openResource(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        return getResourceUrl(name, classLoader).openStream();
    }

    @SuppressWarnings("java:S109")
    static InputStream openResource(@Language("file-reference") String name) {
        val callingClass = getCallingClass(2);
        return openResource(name, callingClass.getClassLoader());
    }

}
