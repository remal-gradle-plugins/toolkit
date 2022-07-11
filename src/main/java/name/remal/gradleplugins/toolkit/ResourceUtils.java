package name.remal.gradleplugins.toolkit;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.reflection.WhoCalledUtils.getCallingClass;

import java.io.InputStream;
import java.net.URL;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.intellij.lang.annotations.Language;

@NoArgsConstructor(access = PRIVATE)
public abstract class ResourceUtils {

    @Nullable
    public static URL findResourceUrl(@Language("file-reference") String name, Class<?> loadingClass) {
        return loadingClass.getResource(name);
    }

    @Nullable
    public static URL findResourceUrl(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        val trueClassLoader = classLoader != null ? classLoader : getSystemClassLoader();
        return trueClassLoader.getResource(name);
    }

    @Nullable
    @SuppressWarnings("java:S109")
    public static URL findResourceUrl(@Language("file-reference") String name) {
        val callingClass = getCallingClass(2);
        return findResourceUrl(name, callingClass.getClassLoader());
    }


    public static URL getResourceUrl(@Language("file-reference") String name, Class<?> loadingClass) {
        val url = findResourceUrl(name, loadingClass);
        if (url == null) {
            throw new IllegalStateException(format(
                "Classpath resource can't be found for %s: %s",
                loadingClass,
                name
            ));
        }
        return url;
    }

    public static URL getResourceUrl(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        val url = findResourceUrl(name, classLoader);
        if (url == null) {
            throw new IllegalStateException(format(
                "Classpath resource can't be found: %s",
                name
            ));
        }
        return url;
    }

    @SuppressWarnings("java:S109")
    public static URL getResourceUrl(@Language("file-reference") String name) {
        val callingClass = getCallingClass(2);
        return getResourceUrl(name, callingClass.getClassLoader());
    }


    @SneakyThrows
    public static InputStream openResource(@Language("file-reference") String name, Class<?> loadingClass) {
        return getResourceUrl(name, loadingClass).openStream();
    }

    @SneakyThrows
    public static InputStream openResource(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        return getResourceUrl(name, classLoader).openStream();
    }

    @SuppressWarnings("java:S109")
    public static InputStream openResource(@Language("file-reference") String name) {
        val callingClass = getCallingClass(2);
        return openResource(name, callingClass.getClassLoader());
    }


    @SneakyThrows
    public static byte[] readResource(@Language("file-reference") String name, Class<?> loadingClass) {
        try (val inputStream = openResource(name, loadingClass)) {
            return toByteArray(inputStream);
        }
    }

    @SneakyThrows
    public static byte[] readResource(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        try (val inputStream = openResource(name, classLoader)) {
            return toByteArray(inputStream);
        }
    }

    @SneakyThrows
    public static byte[] readResource(@Language("file-reference") String name) {
        val callingClass = getCallingClass(2);
        try (val inputStream = openResource(name, callingClass.getClassLoader())) {
            return toByteArray(inputStream);
        }
    }

}
