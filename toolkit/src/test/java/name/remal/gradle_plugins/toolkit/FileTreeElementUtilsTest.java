package name.remal.gradle_plugins.toolkit;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newOutputStream;
import static name.remal.gradle_plugins.toolkit.FileTreeElementUtils.mockedFileTreeElementFromRelativePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.file.RelativePath;
import org.junit.jupiter.api.Nested;
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
        var fileTree = project.files(tempFile).getAsFileTree();
        var isNotArchiveEntry = new AtomicReference<Boolean>();
        fileTree.visit(details -> {
            isNotArchiveEntry.set(FileTreeElementUtils.isNotArchiveEntry(details));
        });
        assertEquals(TRUE, isNotArchiveEntry.get());
    }

    @Test
    void isNotArchive_zip() throws Throwable {
        try (var outputStream = new ZipOutputStream(newOutputStream(tempFile.toPath()))) {
            outputStream.putNextEntry(new ZipEntry("entry"));
            outputStream.write(new byte[]{1, 2, 3});
        }

        var fileTree = project.zipTree(tempFile);
        var isNotArchiveEntry = new AtomicReference<Boolean>();
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


    @Nested
    class MockedFileTreeElementFromRelativePath {

        private final RelativePath relativeDirPath = RelativePath.parse(false, "/dir");
        private final RelativePath relativeFilePath = RelativePath.parse(true, "/dir/file");

        @Test
        void getRelativePath() {
            assertEquals(
                relativeDirPath,
                mockedFileTreeElementFromRelativePath(relativeDirPath).getRelativePath(),
                relativeDirPath.toString()
            );

            assertEquals(
                relativeFilePath,
                mockedFileTreeElementFromRelativePath(relativeFilePath).getRelativePath(),
                relativeFilePath.toString()
            );
        }

        @Test
        void getPath() {
            assertEquals(
                relativeDirPath.getPathString(),
                mockedFileTreeElementFromRelativePath(relativeDirPath).getPath(),
                relativeDirPath.toString()
            );

            assertEquals(
                relativeFilePath.getPathString(),
                mockedFileTreeElementFromRelativePath(relativeFilePath).getPath(),
                relativeFilePath.toString()
            );
        }

        @Test
        void getName() {
            assertEquals(
                relativeDirPath.getLastName(),
                mockedFileTreeElementFromRelativePath(relativeDirPath).getName(),
                relativeDirPath.toString()
            );

            assertEquals(
                relativeFilePath.getLastName(),
                mockedFileTreeElementFromRelativePath(relativeFilePath).getName(),
                relativeFilePath.toString()
            );
        }

        @Test
        void isDirectory() {
            assertTrue(
                mockedFileTreeElementFromRelativePath(relativeDirPath).isDirectory(),
                relativeDirPath.toString()
            );

            assertFalse(
                mockedFileTreeElementFromRelativePath(relativeFilePath).isDirectory(),
                relativeFilePath.toString()
            );
        }

        @Test
        void toString_impl() {
            assertEquals(
                format("mock '%s'", relativeDirPath),
                mockedFileTreeElementFromRelativePath(relativeDirPath).toString(),
                relativeDirPath.toString()
            );

            assertEquals(
                format("mock '%s'", relativeFilePath),
                mockedFileTreeElementFromRelativePath(relativeFilePath).toString(),
                relativeFilePath.toString()
            );
        }


        @Test
        @SuppressWarnings("java:S5778")
        void getFile() {
            assertThrows(
                AbstractMethodError.class,
                () -> mockedFileTreeElementFromRelativePath(relativeDirPath).getFile()
            );

            assertThrows(
                AbstractMethodError.class,
                () -> mockedFileTreeElementFromRelativePath(relativeFilePath).getFile()
            );
        }

    }

}
