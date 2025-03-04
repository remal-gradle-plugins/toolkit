package name.remal.gradle_plugins.toolkit;

import static java.lang.Character.MAX_CODE_POINT;
import static java.lang.Character.MIN_CODE_POINT;
import static java.lang.Character.toChars;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class PropertiesUtilsTest {

    @Test
    void storedStringsAreEscapedCorrectly() throws Throwable {
        var string = new StringBuilder();
        string.append(' ');
        IntStream.rangeClosed(MIN_CODE_POINT, MAX_CODE_POINT).forEach(codePoint -> {
            for (var ch : toChars(codePoint)) {
                string.append(ch);
            }
        });

        var properties = new Properties();
        properties.setProperty(string.toString(), string.toString());

        final byte[] bytes;
        try (var out = new ByteArrayOutputStream()) {
            PropertiesUtils.storeProperties(properties, out);
            bytes = out.toByteArray();
        }

        var parsedProperties = new Properties();
        parsedProperties.load(new ByteArrayInputStream(bytes));

        assertThat(parsedProperties).isEqualTo(properties);
    }

    @Test
    void storedKeysAreSorted() throws Throwable {
        Properties properties = null;
        int maxAttempts = 10_000;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            var currentProperties = new Properties();
            currentProperties.setProperty("a-1", "a");
            currentProperties.setProperty("b-" + attempt, "b");
            var keys = new ArrayList<>(currentProperties.keySet());
            if (keys.get(1).equals("a-1")) {
                properties = currentProperties;
                break;
            }

            if (attempt >= maxAttempts) {
                throw new AssertionError("Can't create properties with not sorted keys");
            }
        }

        final String content;
        try (var writer = new StringWriter()) {
            PropertiesUtils.storeProperties(requireNonNull(properties), writer);
            content = writer.toString().replace(" ", "");
        }

        assertThat(content).startsWith("a-1=");
    }

}
