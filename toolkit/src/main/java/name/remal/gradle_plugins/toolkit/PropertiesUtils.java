package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.newOutputStream;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.UrlUtils.openInputStreamForUrl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class PropertiesUtils {

    @SneakyThrows
    public static Properties loadProperties(URL url) {
        val properties = new Properties();
        try (val inputStream = openInputStreamForUrl(url)) {
            properties.load(inputStream);
        }
        return properties;
    }

    @SneakyThrows
    public static Properties loadProperties(URI uri) {
        return loadProperties(uri.toURL());
    }

    public static Properties loadProperties(Path path) {
        return loadProperties(normalizePath(path).toUri());
    }

    public static Properties loadProperties(File file) {
        return loadProperties(file.toPath());
    }


    @SneakyThrows
    public static void storeProperties(Properties properties, Path path) {
        path = normalizePath(path);
        createParentDirectories(path);
        try (val outputStream = new StripFirstLineOutputStream(newOutputStream(path))) {
            properties.store(outputStream, null);
        }
    }

    public static void storeProperties(Properties properties, File file) {
        storeProperties(properties, file.toPath());
    }

    @SuppressWarnings("java:S4349")
    private static class StripFirstLineOutputStream extends FilterOutputStream {

        public StripFirstLineOutputStream(OutputStream out) {
            super(new BufferedOutputStream(out));
        }

        private boolean firstLineSeparatorStarted = false;
        private boolean firstLineSeen = false;

        @Override
        public void write(int b) throws IOException {
            if (firstLineSeparatorStarted && !isLineSeparator(b)) {
                firstLineSeen = true;
            }

            if (firstLineSeen) {
                super.write(b);

            } else if (isLineSeparator(b)) {
                firstLineSeparatorStarted = true;
            }
        }

        private static boolean isLineSeparator(int b) {
            return b == '\n' || b == '\r';
        }

    }

}
