package name.remal.gradle_plugins.toolkit;

import static com.google.common.io.ByteStreams.toByteArray;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.InputOutputStreamUtils.withOnClose;
import static name.remal.gradle_plugins.toolkit.UriUtils.toUri;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class UrlUtils {

    @SneakyThrows
    public static URL parseUrl(String string) {
        return new URL(string);
    }

    @SneakyThrows
    public static URL toUrl(URI uri) {
        return uri.toURL();
    }

    public static URL toUrl(File file) {
        return toUrl(toUri(file));
    }

    public static URL toUrl(Path path) {
        return toUrl(toUri(path));
    }

    @MustBeClosed
    @SneakyThrows
    public static InputStream openInputStreamForUrl(URL url) {
        val connection = url.openConnection();
        connection.setUseCaches(false);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(60000);
        return withOnClose(connection.getInputStream(), __ -> {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
        });
    }

    @SneakyThrows
    public static byte[] readBytesFromUrl(URL url) {
        try (val inputStream = openInputStreamForUrl(url)) {
            return toByteArray(inputStream);
        }
    }

    public static String readStringFromUrl(URL url, Charset charset) {
        val bytes = readBytesFromUrl(url);
        return new String(bytes, charset);
    }

}
