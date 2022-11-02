package name.remal.gradleplugins.toolkit;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

class FileTreeElementUtilsTest {

    private final Project project;
    private final File tempFile;

    @SneakyThrows
    public FileTreeElementUtilsTest(Project project) {
        this.project = project;
        this.tempFile = createTempFile("file-", ".temp").toFile();
    }


    @Test
    void isNotArchive_file() {
        val fileTree = project.files(tempFile).getAsFileTree();
        val isNotArchiveEntry = new AtomicReference<Boolean>();
        fileTree.visit(details -> {
            isNotArchiveEntry.set(FileTreeElementUtils.isNotArchiveEntry(details));
        });
        assertEquals(TRUE, isNotArchiveEntry.get());
    }

    @Test
    void isNotArchive_zip() throws Throwable {
        try (val outputStream = new ZipOutputStream(newOutputStream(tempFile.toPath()))) {
            outputStream.putNextEntry(new ZipEntry("entry"));
            outputStream.write(new byte[]{1, 2, 3});
        }

        val fileTree = project.zipTree(tempFile);
        val isNotArchiveEntry = new AtomicReference<Boolean>();
        fileTree.visit(details -> {
            isNotArchiveEntry.set(FileTreeElementUtils.isNotArchiveEntry(details));
        });
        assertEquals(FALSE, isNotArchiveEntry.get());
    }

    @Test
    void abstract_archive_file_tree_classes() {
        assertFalse(
            FileTreeElementUtils.ABSTRACT_ARCHIVE_FILE_TREE_CLASSES.isEmpty(),
            "ABSTRACT_ARCHIVE_FILE_TREE_CLASSES is empty"
        );
    }

}
