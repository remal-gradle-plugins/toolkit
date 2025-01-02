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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.annotation.WillClose;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class PropertiesUtils {

    public static Properties loadProperties(File file) {
        return loadProperties(file.toPath());
    }

    @SneakyThrows
    public static Properties loadProperties(Path path) {
        path = normalizePath(path);
        try (val inputStream = newInputStream(path)) {
            return loadProperties(inputStream);
        }
    }

    @SneakyThrows
    public static Properties loadProperties(URI uri) {
        return loadProperties(uri.toURL());
    }

    @SneakyThrows
    public static Properties loadProperties(URL url) {
        try (val inputStream = openInputStreamForUrl(url)) {
            return loadProperties(inputStream);
        }
    }

    @SneakyThrows
    public static Properties loadProperties(@WillClose InputStream inputStream) {
        try (val reader = new InputStreamReader(inputStream, UTF_8)) {
            return loadProperties(reader);
        }
    }

    @SneakyThrows
    public static Properties loadProperties(@WillClose Reader reader) {
        val properties = new Properties();
        try (val br = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
            properties.load(br);
        }
        return properties;
    }


    public static void storeProperties(Map<Object, Object> properties, File file) {
        storeProperties(properties, file.toPath());
    }

    @SneakyThrows
    public static void storeProperties(Map<Object, Object> properties, Path path) {
        path = normalizePath(path);
        createParentDirectories(path);
        try (val outputStream = newOutputStream(path)) {
            storeProperties(properties, outputStream);
        }
    }

    @SneakyThrows
    public static void storeProperties(Map<Object, Object> properties, @WillClose OutputStream outputStream) {
        try (val writer = new OutputStreamWriter(outputStream, ISO_8859_1)) {
            storeProperties(properties, writer);
        }
    }

    @SneakyThrows
    public static void storeProperties(Map<Object, Object> properties, @WillClose Writer writer) {
        try (val bw = writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer)) {
            val map = new TreeMap<String, String>();
            properties.forEach((key, value) -> map.put(key.toString(), value.toString()));
            for (val entry : map.entrySet()) {
                writeEscaped(entry.getKey(), true, bw);
                bw.append('=');
                writeEscaped(entry.getValue(), false, bw);
                bw.append('\n');
            }
        }
    }

    private static void writeEscaped(String string, boolean escapeSpace, Writer writer) throws IOException {
        for (int index = 0; index < string.length(); index++) {
            val ch = string.charAt(index);
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

}
