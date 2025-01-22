package name.remal.gradle_plugins.toolkit;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchiveUtilsTest {

    @Test
    void newZipArchiveWriter(@TempDir File tempDir) throws Throwable {
        var archiveFile = new File(tempDir, "archive.zip");
        var entryName = "dir/entry";
        var entryContent = new byte[]{1, 2, 3};
        try (var archiveWriter = ArchiveUtils.newZipArchiveWriter(archiveFile)) {
            archiveWriter.writeEntry(entryName, entryContent);
        }

        try (var zipFile = new ZipFile(archiveFile, UTF_8)) {
            var actualEntry = zipFile.getEntry(entryName);
            assertNotNull(actualEntry, "actualEntry");
            assertFalse(actualEntry.isDirectory(), "isDirectory");
            var actualContent = toByteArray(zipFile.getInputStream(actualEntry));
            assertArrayEquals(entryContent, actualContent, "content");
        }
    }

}
