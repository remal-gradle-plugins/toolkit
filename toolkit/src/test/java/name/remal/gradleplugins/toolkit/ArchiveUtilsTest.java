package name.remal.gradleplugins.toolkit;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.zip.ZipFile;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchiveUtilsTest {

    @Test
    void newZipArchiveWriter(@TempDir File tempDir) throws Throwable {
        val archiveFile = new File(tempDir, "archive.zip");
        val entryName = "dir/entry";
        val entryContent = new byte[]{1, 2, 3};
        try (val archiveWriter = ArchiveUtils.newZipArchiveWriter(archiveFile)) {
            archiveWriter.writeEntry(entryName, entryContent);
        }

        try (val zipFile = new ZipFile(archiveFile, UTF_8)) {
            val actualEntry = zipFile.getEntry(entryName);
            assertNotNull(actualEntry, "actualEntry");
            assertFalse(actualEntry.isDirectory(), "isDirectory");
            val actualContent = toByteArray(zipFile.getInputStream(actualEntry));
            assertArrayEquals(entryContent, actualContent, "content");
        }
    }

}
