package name.remal.gradle_plugins.toolkit;

import static java.lang.ClassLoader.getSystemClassLoader;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.UrlUtils.openInputStreamForUrl;
import static name.remal.gradle_plugins.toolkit.UrlUtils.readBytesFromUrl;
import static name.remal.gradle_plugins.toolkit.UrlUtils.readStringFromUrl;
import static name.remal.gradle_plugins.toolkit.reflection.WhoCalledUtils.getCallingClass;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.Language;

@NoArgsConstructor(access = PRIVATE)
public abstract class ResourceUtils {

    @Nullable
    public static URL findResourceUrl(@Language("file-reference") String name, Class<?> loadingClass) {
        URL url = loadingClass.getResource(name);
        if (url == null) {
            url = findResourceUrl(name, loadingClass.getClassLoader());
        }
        return url;
    }

    @Nullable
    public static URL findResourceUrl(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        var trueClassLoader = classLoader != null ? classLoader : getSystemClassLoader();
        return trueClassLoader.getResource(name);
    }

    @Nullable
    public static URL findResourceUrl(@Language("file-reference") String name) {
        var callingClass = getCallingClass(2);
        return findResourceUrl(name, callingClass);
    }


    public static URL getResourceUrl(@Language("file-reference") String name, Class<?> loadingClass) {
        var url = findResourceUrl(name, loadingClass);
        if (url == null) {
            throw new ResourceNotFoundException(
                loadingClass,
                name
            );
        }
        return url;
    }

    public static URL getResourceUrl(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        var url = findResourceUrl(name, classLoader);
        if (url == null) {
            throw new ResourceNotFoundException(name);
        }
        return url;
    }

    public static URL getResourceUrl(@Language("file-reference") String name) {
        var callingClass = getCallingClass(2);
        return getResourceUrl(name, callingClass);
    }


    @MustBeClosed
    public static InputStream openResource(@Language("file-reference") String name, Class<?> loadingClass) {
        var url = getResourceUrl(name, loadingClass);
        return openInputStreamForUrl(url);
    }

    @MustBeClosed
    public static InputStream openResource(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        var url = getResourceUrl(name, classLoader);
        return openInputStreamForUrl(url);
    }

    @MustBeClosed
    public static InputStream openResource(@Language("file-reference") String name) {
        var callingClass = getCallingClass(2);
        var url = getResourceUrl(name, callingClass);
        return openInputStreamForUrl(url);
    }


    public static byte[] readResource(@Language("file-reference") String name, Class<?> loadingClass) {
        var url = getResourceUrl(name, loadingClass);
        return readBytesFromUrl(url);
    }

    public static byte[] readResource(@Language("file-reference") String name, @Nullable ClassLoader classLoader) {
        var url = getResourceUrl(name, classLoader);
        return readBytesFromUrl(url);
    }

    public static byte[] readResource(@Language("file-reference") String name) {
        var callingClass = getCallingClass(2);
        var url = getResourceUrl(name, callingClass);
        return readBytesFromUrl(url);
    }


    public static String readTextResource(
        @Language("file-reference") String name,
        Charset charset,
        Class<?> loadingClass
    ) {
        var url = getResourceUrl(name, loadingClass);
        return readStringFromUrl(url, charset);
    }

    public static String readTextResource(
        @Language("file-reference") String name,
        Charset charset,
        @Nullable ClassLoader classLoader
    ) {
        var url = getResourceUrl(name, classLoader);
        return readStringFromUrl(url, charset);
    }

    public static String readTextResource(
        @Language("file-reference") String name,
        Charset charset
    ) {
        var callingClass = getCallingClass(2);
        var url = getResourceUrl(name, callingClass);
        return readStringFromUrl(url, charset);
    }

}
