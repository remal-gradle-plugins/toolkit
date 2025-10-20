package name.remal.gradle_plugins.toolkit.internal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import groovy.lang.GString;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.codehaus.groovy.runtime.GStringImpl;
import org.junit.jupiter.api.Test;

class GsonTypeAdaptersTest {

    final Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(new GsonTypeAdapters())
        .create();

    @Test
    void gstring() {
        var value = new GStringImpl(new Object[0], new String[]{"value"});
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, GString.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void charset() {
        var value = UTF_8;
        var json = gson.toJson(value);
        assertEquals('"' + value.name() + '"', json);
        var deserializedValue = gson.fromJson(json, Charset.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void file() {
        var value = new File("test");
        var json = gson.toJson(value);
        assertEquals('"' + normalizeFile(value).getPath().replace("\\", "\\\\") + '"', json);
        var deserializedValue = gson.fromJson(json, File.class);
        assertEquals(normalizeFile(value), deserializedValue);
    }

    @Test
    void path() {
        var value = Paths.get("test");
        var json = gson.toJson(value);
        assertEquals('"' + normalizePath(value).toString().replace("\\", "\\\\") + '"', json);
        var deserializedValue = gson.fromJson(json, Path.class);
        assertEquals(normalizePath(value), deserializedValue);
    }

    @Test
    void zoneId() {
        var value = ZoneId.of("America/New_York");
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, ZoneId.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void zoneOffset() {
        var value = ZoneOffset.of("+14:31");
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, ZoneOffset.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void localDate() {
        var value = LocalDate.of(2025, 12, 31);
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, LocalDate.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void localTime() {
        var value = LocalTime.of(23, 13, 11);
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, LocalTime.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void localDateTime() {
        var value = LocalDateTime.of(
            LocalDate.of(2025, 12, 31),
            LocalTime.of(23, 13, 11)
        );
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, LocalDateTime.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void offsetTime() {
        var value = OffsetTime.of(
            LocalTime.of(23, 13, 11),
            ZoneOffset.of("+14:31")
        );
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, OffsetTime.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void duration() {
        var value = Duration.ofMinutes(43);
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, Duration.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void period() {
        var value = Period.ofMonths(11);
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, Period.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void zonedDateTime() {
        var value = ZonedDateTime.of(
            LocalDate.of(2025, 12, 31),
            LocalTime.of(23, 13, 11),
            ZoneId.of("America/New_York")
        );
        var json = gson.toJson(value);
        assertEquals('"' + value.toString() + '"', json);
        var deserializedValue = gson.fromJson(json, ZonedDateTime.class);
        assertEquals(value, deserializedValue);
    }

    @Test
    void offsetDateTime() {
        {
            var value = OffsetDateTime.of(
                LocalDate.of(2025, 12, 31),
                LocalTime.of(23, 13, 11),
                ZoneOffset.of("+14:31")
            );
            var json = gson.toJson(value);
            assertEquals('"' + value.toString() + '"', json);
            var deserializedValue = gson.fromJson(json, OffsetDateTime.class);
            assertEquals(value, deserializedValue);
        }

        {
            var value = ZonedDateTime.of(
                LocalDate.of(2025, 12, 31),
                LocalTime.of(23, 13, 11),
                ZoneId.of("America/New_York")
            );
            var json = gson.toJson(value);
            assertEquals('"' + value.toString() + '"', json);
            var deserializedValue = gson.fromJson(json, OffsetDateTime.class);
            assertEquals(value.toOffsetDateTime(), deserializedValue);
        }
    }

    @Test
    void instant() {
        {
            var value = Instant.ofEpochSecond(35214343);
            var json = gson.toJson(value);
            assertEquals('"' + value.toString() + '"', json);
            var deserializedValue = gson.fromJson(json, Instant.class);
            assertEquals(value, deserializedValue);
        }

        {
            var value = OffsetDateTime.of(
                LocalDate.of(2025, 12, 31),
                LocalTime.of(23, 13, 11),
                ZoneOffset.of("+14:31")
            );
            var json = gson.toJson(value);
            assertEquals('"' + value.toString() + '"', json);
            var deserializedValue = gson.fromJson(json, Instant.class);
            assertEquals(value.toInstant(), deserializedValue);
        }

        {
            var value = ZonedDateTime.of(
                LocalDate.of(2025, 12, 31),
                LocalTime.of(23, 13, 11),
                ZoneId.of("America/New_York")
            );
            var json = gson.toJson(value);
            assertEquals('"' + value.toString() + '"', json);
            var deserializedValue = gson.fromJson(json, Instant.class);
            assertEquals(value.toInstant(), deserializedValue);
        }
    }

    @Test
    void optional() {
        {
            var value = Optional.empty();
            var json = gson.toJson(value);
            assertEquals("null", json);
            var deserializedValue = gson.fromJson(json, new TypeToken<Optional<String>>() { });
            assertEquals(value, deserializedValue);
        }

        {
            var value = Optional.of("value");
            var json = gson.toJson(value);
            assertEquals('"' + value.get() + '"', json);
            var deserializedValue = gson.fromJson(json, new TypeToken<Optional<String>>() { });
            assertEquals(value, deserializedValue);
        }
    }

}
