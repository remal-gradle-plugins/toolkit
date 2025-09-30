package name.remal.gradle_plugins.toolkit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newOutputStream;
import static java.util.zip.Deflater.BEST_COMPRESSION;
import static java.util.zip.Deflater.DEFLATED;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
public abstract class ArchiveUtils {

    @MustBeClosed
    @SneakyThrows
    public static ZipOutputStream newZipOutputStream(Path path) {
        path = normalizePath(path);
        createParentDirectories(path);

        var outputStream = newOutputStream(path);
        var zipOutputStream = new ZipOutputStream(outputStream, UTF_8);
        zipOutputStream.setMethod(DEFLATED);
        zipOutputStream.setLevel(BEST_COMPRESSION);

        return zipOutputStream;
    }

    @MustBeClosed
    public static ZipOutputStream newZipOutputStream(File file) {
        return newZipOutputStream(file.toPath());
    }


    @SneakyThrows
    @SuppressWarnings({"try", "EmptyTryBlock"})
    public static Path newEmptyZipArchive(Path path) {
        path = normalizePath(path);
        try (var out = newZipOutputStream(path)) {
            // empty ZIP archive
        }
        return path;
    }

    public static File newEmptyZipArchive(File file) {
        return newEmptyZipArchive(file.toPath()).toFile();
    }


    @MustBeClosed
    @SneakyThrows
    @SuppressWarnings({"java:S2095", "resource", "MustBeClosedChecker"})
    public static ArchiveWriter newZipArchiveWriter(Path path) {
        var zipOutputStream = newZipOutputStream(path);
        return new ArchiveWriter() {
            @Override
            public void writeEntry(String entryName, byte[] bytes) throws IOException {
                zipOutputStream.putNextEntry(new ZipEntry(entryName));
                zipOutputStream.write(bytes);
            }

            @Override
            public void close() throws IOException {
                zipOutputStream.close();
            }
        };
    }

    @MustBeClosed
    public static ArchiveWriter newZipArchiveWriter(File file) {
        return newZipArchiveWriter(file.toPath());
    }

}
