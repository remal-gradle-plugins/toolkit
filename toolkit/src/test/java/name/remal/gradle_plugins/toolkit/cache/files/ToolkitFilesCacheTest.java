package name.remal.gradle_plugins.toolkit.cache.files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static name.remal.gradle_plugins.toolkit.PropertiesUtils.storePropertiesToString;
import static name.remal.gradle_plugins.toolkit.cache.files.ToolkitFilesCacheFieldBinary.binaryToolkitFilesCacheField;
import static name.remal.gradle_plugins.toolkit.cache.files.ToolkitFilesCacheFieldProperties.propertiesToolkitFilesCacheField;
import static name.remal.gradle_plugins.toolkit.testkit.ModifiableClock.modifiableClock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jetbrains.kotlin.org.apache.commons.codec.binary.Hex.encodeHexString;

import com.google.common.jimfs.Jimfs;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.testkit.ModifiableClock;
import org.junit.jupiter.api.Test;

class ToolkitFilesCacheTest {

    final FileSystem fileSystem = Jimfs.newFileSystem();
    final Path rootPath = fileSystem.getRootDirectories().iterator().next();

    final Path cacheDir = rootPath.resolve("cache");
    final Duration lastAccessRetention = Duration.ofDays(1);
    final ModifiableClock clock = modifiableClock();

    private static final ToolkitFilesCacheFieldBinary CONTENT = binaryToolkitFilesCacheField("content");
    private static final ToolkitFilesCacheFieldProperties PROPERTIES = propertiesToolkitFilesCacheField("properties");

    final ToolkitFilesCache<Map<String, String>> cache = ToolkitFilesCache.<Map<String, String>>builder()
        .baseDir(cacheDir)
        .keyEncoder(ToolkitFilesCacheTest::encodeKey)
        .fields(List.of(CONTENT, PROPERTIES))
        .lastAccessRetention(lastAccessRetention)
        .clock(clock)
        .build();

    @Test
    void withoutValueAccess() {
        var key = Map.of("key", "value");
        cache.withValue(key, value -> {
            // do nothing
        });

        var encodedKey = encodeKey(key);
        var keyHash = hashKey(encodedKey);
        assertThat(getKeyDirPath(keyHash))
            .doesNotExist();
    }

    @Test
    void updateMetadata() {
        var key = Map.of("key", "value");
        cache.withValue(key, value -> {
            value.set(CONTENT, new byte[]{1, 2, 3});
        });

        var encodedKey = encodeKey(key);
        var keyHash = hashKey(encodedKey);
        assertThat(getKeyDirPath(keyHash).resolve("sys-metadata"))
            .isRegularFile()
            .content(UTF_8)
            .isEqualTo(storePropertiesToString(Map.of(
                "createdAt", clock.instant().toString(),
                "accessedAt", clock.instant().toString(),
                "encodedKey", encodedKey
            )));
    }

    @Test
    void updateAccessedAt() {
        var key = Map.of("key", "value");
        cache.withValue(key, value -> {
            value.set(CONTENT, new byte[]{1, 2, 3});
        });

        var prevInstant = clock.instant();
        clock.tick(Duration.ofSeconds(1));
        var newInstant = clock.instant();

        cache.withValue(key, value -> {
            value.set(CONTENT, new byte[]{4, 5, 6});
        });

        var encodedKey = encodeKey(key);
        var keyHash = hashKey(encodedKey);
        assertThat(getKeyDirPath(keyHash).resolve("sys-metadata"))
            .isRegularFile()
            .content(UTF_8)
            .isEqualTo(storePropertiesToString(Map.of(
                "createdAt", prevInstant.toString(),
                "accessedAt", newInstant.toString(),
                "encodedKey", encodedKey
            )));
    }

    @Test
    void writeBinary() {
        var key = Map.of("key", "value");
        cache.withValue(key, value -> {
            value.set(CONTENT, new byte[]{1, 2, 3});
        });

        var encodedKey = encodeKey(key);
        var keyHash = hashKey(encodedKey);
        assertThat(getKeyDirPath(keyHash).resolve(CONTENT.getId()))
            .isRegularFile()
            .binaryContent()
            .isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    void writeProperties() {
        var key = Map.of("key", "value");
        cache.withValue(key, value -> {
            var props = new Properties();
            props.setProperty("propName", "propValue");
            value.set(PROPERTIES, props);
        });

        var encodedKey = encodeKey(key);
        var keyHash = hashKey(encodedKey);
        assertThat(getKeyDirPath(keyHash).resolve(PROPERTIES.getId()))
            .isRegularFile()
            .content(UTF_8)
            .isEqualTo(storePropertiesToString(Map.of(
                "propName", "propValue"
            )));
    }

    @Test
    void cleanupForFreshFilesDeleteNothing() {
        var key = Map.of("key", "value");
        cache.withValue(key, value -> {
            value.set(CONTENT, new byte[]{1, 2, 3});
        });

        cache.cleanup();

        var encodedKey = encodeKey(key);
        var keyHash = hashKey(encodedKey);
        assertThat(getKeyDirPath(keyHash))
            .isDirectory();
    }

    @Test
    void cleanupForOldFilesDeleteFiles() {
        var key = Map.of("key", "value");
        cache.withValue(key, value -> {
            value.set(CONTENT, new byte[]{1, 2, 3});
        });

        clock.tick(lastAccessRetention.plusSeconds(1));
        cache.cleanup();

        var encodedKey = encodeKey(key);
        var keyHash = hashKey(encodedKey);
        assertThat(getKeyDirPath(keyHash))
            .doesNotExist();
    }


    private static String encodeKey(Map<String, String> key) {
        return new TreeMap<>(key).toString();
    }

    @SneakyThrows
    private static String hashKey(String encodedKey) {
        var md = MessageDigest.getInstance("SHA-256");
        var digest = md.digest(encodedKey.getBytes(UTF_8));
        return encodeHexString(digest);
    }

    private Path getKeyDirPath(String keyHash) {
        return cacheDir.resolve(keyHash.substring(0, 2)).resolve(keyHash);
    }

}
