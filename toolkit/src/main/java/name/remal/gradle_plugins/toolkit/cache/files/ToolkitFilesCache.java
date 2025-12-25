package name.remal.gradle_plugins.toolkit.cache.files;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.list;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.util.Collections.shuffle;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.deleteRecursively;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.PathUtils.withShortExclusiveLock;
import static name.remal.gradle_plugins.toolkit.PropertiesUtils.storeProperties;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

@SuperBuilder
public final class ToolkitFilesCache<KEY> extends ToolkitFilesCacheParams<KEY> {

    private static final String SEGMENT_LOCK_FILE_NAME = ".lock";
    private static final String METADATA_FIELD_ID = "sys-metadata";

    private final Path normalizedBaseDir = normalizePath(baseDir);

    {
        validate();
        cleanup();
    }

    private void validate() {
        var registeredValues = new LinkedHashSet<String>();
        for (var value : fields) {
            var id = value.getId();
            if (!registeredValues.add(id)) {
                throw new IllegalStateException("Duplicate cache value ID: " + id);
            }
        }
    }


    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public void withValue(KEY key, ToolkitFilesCacheValueAction action) {
        var encodedKey = keyEncoder.encode(key);
        var keyHash = hashKey(encodedKey);
        var segment = keyHash.substring(0, 2);
        var segmentDirPath = normalizedBaseDir.resolve(segment);
        withShortExclusiveLock(segmentDirPath.resolve(SEGMENT_LOCK_FILE_NAME), () -> {
            var keyDirPath = segmentDirPath.resolve(keyHash);

            var metadataFilePath = keyDirPath.resolve(METADATA_FIELD_ID);
            var metadata = loadMetadata(metadataFilePath);
            var lockedAt = clock.instant();
            if (metadata.getProperty("createdAt") == null) {
                metadata.setProperty("createdAt", lockedAt.toString());
            }
            if (metadata.getProperty("encodedKey") == null) {
                metadata.setProperty("encodedKey", encodedKey);
            }
            var prevEncodedKey = requireNonNull(metadata.getProperty("encodedKey"));
            if (!encodedKey.equals(prevEncodedKey)) {
                throw new IllegalStateException(format(
                    "Hash collision for keys '%s' and '%s' with hash '%s'",
                    encodedKey,
                    prevEncodedKey,
                    keyHash
                ));
            }

            Runnable updateAccessedAt = () -> {
                var accessedAt = clock.instant();
                if (accessedAt.compareTo(lockedAt) <= 0) {
                    accessedAt = lockedAt;
                }
                metadata.setProperty("accessedAt", accessedAt.toString());
                storeProperties(metadata, metadataFilePath);
            };

            var value = new ToolkitFilesCacheValue() {
                @Override
                @Nullable
                @SneakyThrows
                public <T> T get(ToolkitFilesCacheField<T> field) {
                    checkFieldRegistered(field);
                    updateAccessedAt.run();

                    var fieldFilePath = keyDirPath.resolve(field.getId());
                    if (!exists(fieldFilePath)) {
                        return null;
                    }

                    var bytes = readAllBytes(fieldFilePath);
                    var fieldValue = field.deserialize(bytes);
                    return field.getType().cast(fieldValue);
                }

                @Override
                @SneakyThrows
                public <T> ToolkitFilesCacheValue set(ToolkitFilesCacheField<T> field, @Nullable T value) {
                    checkFieldRegistered(field);
                    updateAccessedAt.run();

                    var fieldFilePath = keyDirPath.resolve(field.getId());
                    if (value == null) {
                        deleteIfExists(fieldFilePath);

                    } else {
                        var bytes = field.serialize(value);
                        if (bytes == null) {
                            deleteIfExists(fieldFilePath);
                        } else {
                            createParentDirectories(fieldFilePath);
                            write(fieldFilePath, bytes);
                        }
                    }

                    return this;
                }
            };
            action.execute(value);
        });
    }

    @SneakyThrows
    private static String hashKey(String encodedKey) {
        var md = MessageDigest.getInstance("SHA-256");
        var digest = md.digest(encodedKey.getBytes(UTF_8));
        return encodeHexString(digest);
    }

    @SneakyThrows
    private static Properties loadMetadata(Path metadataFilePath) {
        var metadata = new Properties();
        if (exists(metadataFilePath)) {
            try (var reader = newBufferedReader(metadataFilePath, UTF_8)) {
                metadata.load(reader);
            }
        }
        return metadata;
    }

    private void checkFieldRegistered(ToolkitFilesCacheField<?> field) {
        if (!fields.contains(field)) {
            throw new IllegalArgumentException(format(
                "Field is not registered in cache: %s."
                    + " It should be the same instance of the field as used during cache creation.",
                field.getId()
            ));
        }
    }


    //#region Cleanup

    private static final Duration MAX_SEGMENT_CLEANUP_DURATION = Duration.ofMillis(100);

    @VisibleForTesting
    void cleanup() {
        try {
            listFolderInRandomOrder(normalizedBaseDir).stream()
                .filter(Files::isDirectory)
                .forEach(this::cleanupSegment);
        } catch (IOException e) {
            // do nothing
        }
    }

    private void cleanupSegment(Path segmentDirPath) {
        var startNanos = nanoTime();
        var endNanos = startNanos + MAX_SEGMENT_CLEANUP_DURATION.toNanos();
        withShortExclusiveLock(segmentDirPath.resolve(SEGMENT_LOCK_FILE_NAME), () -> {
            try {
                if (nanoTime() > endNanos) {
                    return;
                }

                listFolderInRandomOrder(segmentDirPath).stream()
                    .filter(Files::isDirectory)
                    .forEach(this::cleanupKey);
            } catch (IOException e) {
                // do nothing
            }
        });
    }

    @SneakyThrows
    private void cleanupKey(Path keyDirPath) {
        var metadataFilePath = keyDirPath.resolve(METADATA_FIELD_ID);
        var metadata = loadMetadata(metadataFilePath);
        var accessedAtString = metadata.getProperty("accessedAt");
        if (accessedAtString == null) {
            accessedAtString = metadata.getProperty("createdAt");
        }
        if (accessedAtString != null) {
            var accessedAt = Instant.parse(accessedAtString);
            var minAccessedAt = clock.instant().minus(lastAccessRetention);
            if (accessedAt.compareTo(minAccessedAt) >= 0) {
                return;
            }
        }

        deleteRecursively(keyDirPath);
    }

    private static List<Path> listFolderInRandomOrder(Path dirPath) throws IOException {
        try (var stream = list(dirPath)) {
            var list = stream.collect(toCollection(ArrayList::new));
            shuffle(list);
            return list;
        }
    }

    //#endregion

}
