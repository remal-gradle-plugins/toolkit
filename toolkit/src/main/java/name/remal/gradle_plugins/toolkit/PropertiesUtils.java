package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.UrlUtils.openInputStreamForUrl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.annotation.WillNotClose;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("java:S2093")
public abstract class PropertiesUtils {

    public static Properties loadProperties(File file) {
        return loadProperties(file.toPath());
    }

    @SneakyThrows
    public static Properties loadProperties(Path path) {
        path = normalizePath(path);
        try (var inputStream = newInputStream(path)) {
            return loadProperties(inputStream);
        }
    }

    @SneakyThrows
    public static Properties loadProperties(URI uri) {
        return loadProperties(uri.toURL());
    }

    @SneakyThrows
    public static Properties loadProperties(URL url) {
        try (var inputStream = openInputStreamForUrl(url)) {
            return loadProperties(inputStream);
        }
    }

    @SneakyThrows
    public static Properties loadProperties(@WillNotClose InputStream inputStream) {
        var reader = new InputStreamReader(inputStream, UTF_8);
        return loadProperties(reader);
    }

    @SneakyThrows
    public static Properties loadProperties(@WillNotClose Reader reader) {
        var properties = new Properties();
        reader = reader instanceof BufferedReader
            ? (BufferedReader) reader
            : new BufferedReader(reader);
        properties.load(reader);
        return properties;
    }


    public static void storeProperties(Map<?, ?> properties, File file) {
        storeProperties(properties, file.toPath());
    }

    @SneakyThrows
    public static void storeProperties(Map<?, ?> properties, Path path) {
        path = normalizePath(path);
        createParentDirectories(path);
        try (var outputStream = newOutputStream(path)) {
            storeProperties(properties, outputStream);
        }
    }

    @SneakyThrows
    public static void storeProperties(Map<?, ?> properties, @WillNotClose OutputStream outputStream) {
        var writer = new OutputStreamWriter(outputStream, ISO_8859_1);
        storeProperties(properties, writer);
    }

    @SneakyThrows
    public static void storeProperties(Map<?, ?> properties, @WillNotClose Writer writer) {
        writer = writer instanceof BufferedWriter
            ? (BufferedWriter) writer
            : new BufferedWriter(writer);
        try {
            var map = new TreeMap<String, String>();
            properties.forEach((key, value) -> map.put(key.toString(), value.toString()));
            for (var entry : map.entrySet()) {
                writeEscaped(entry.getKey(), true, writer);
                writer.append('=');
                writeEscaped(entry.getValue(), false, writer);
                writer.append('\n');
            }
        } finally {
            writer.flush();
        }
    }

    private static void writeEscaped(
        String string,
        boolean escapeSpace,
        @WillNotClose Writer writer
    ) throws IOException {
        for (int index = 0; index < string.length(); index++) {
            var ch = string.charAt(index);
            if (ch == '\\'
                || ch == '='
                || ch == ':'
                || ch == '#'
                || ch == '!'
                || (ch == ' ' && escapeSpace)
                || (ch == ' ' && index == 0)
            ) {
                writer.append('\\').append(ch);

            } else if (ch == '\t') {
                writer.append("\\t");
            } else if (ch == '\n') {
                writer.append("\\n");
            } else if (ch == '\r') {
                writer.append("\\r");
            } else if (ch == '\f') {
                writer.append("\\f");

            } else if (ch <= 31 || 127 <= ch) {
                writer.append("\\u").append(format("%04x", (int) ch).toUpperCase());

            } else {
                writer.append(ch);
            }
        }
    }


    @Contract(pure = true)
    @SneakyThrows
    public static byte[] storePropertiesToBytes(Map<?, ?> properties) {
        try (var out = new ByteArrayOutputStream()) {
            storeProperties(properties, out);
            return out.toByteArray();
        }
    }

    @Contract(pure = true)
    @SneakyThrows
    public static String storePropertiesToString(Map<?, ?> properties) {
        try (var writer = new StringWriter()) {
            storeProperties(properties, writer);
            return writer.toString();
        }
    }

}
