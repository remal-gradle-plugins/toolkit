package name.remal.gradle_plugins.toolkit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newOutputStream;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class ArchiveUtils {

    @MustBeClosed
    @SneakyThrows
    @SuppressWarnings("java:S2095")
    public static ArchiveWriter newZipArchiveWriter(Path path) {
        path = normalizePath(path);
        createParentDirectories(path);

        val outputStream = newOutputStream(path);
        val zipOutputStream = new ZipOutputStream(outputStream, UTF_8);
        zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
        zipOutputStream.setLevel(Deflater.BEST_COMPRESSION);
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
