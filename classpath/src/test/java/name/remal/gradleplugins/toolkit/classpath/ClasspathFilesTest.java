package name.remal.gradleplugins.toolkit.classpath;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.newOutputStream;
import static name.remal.gradleplugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradleplugins.toolkit.PathUtils.deleteRecursively;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("InjectedReferences")
class ClasspathFilesTest {

    private final File tempDir;

    @SneakyThrows
    ClasspathFilesTest() {
        tempDir = createTempDirectory(ClasspathFilesTest.class.getSimpleName() + '-').toFile();
    }

    @AfterEach
    void testIfFileIsNotLocked() {
        deleteRecursively(tempDir.toPath());
    }


    @Nested
    class DirectoryClasspathFile {

        private final String resourceName = "pkg/resource.txt";

        private final ClasspathFiles classpathFiles;

        @SneakyThrows
        DirectoryClasspathFile() {
            val file = new File(tempDir, "dir");
            val resource = new File(file, resourceName);
            createParentDirectories(resource.toPath());
            try (val fileStream = newOutputStream(resource.toPath())) {
                fileStream.write(new byte[]{1, 2, 3}, 0, 3);
            }
            classpathFiles = new ClasspathFiles(List.of(file));
        }

        @Test
        void getResourceNames() {
            assertEquals(List.of(resourceName), List.copyOf(classpathFiles.getResourceNames()));
        }

        @Test
        void openStream() throws IOException {
            try (val inputStream = classpathFiles.openStream(resourceName)) {
                assertNotNull(inputStream);
                assertArrayEquals(new byte[]{1, 2, 3}, toByteArray(inputStream));
            }
        }

    }


    @Nested
    class JarClasspathFile {

        private final String resourceName = "pkg/resource.txt";

        private final ClasspathFiles classpathFiles;

        @SneakyThrows
        JarClasspathFile() {
            val file = new File(tempDir, "artifact.jar");
            createParentDirectories(file.toPath());
            try (val fileStream = newOutputStream(file.toPath())) {
                try (val zipStream = new ZipOutputStream(fileStream)) {
                    zipStream.putNextEntry(new ZipEntry("pkg/"));

                    zipStream.putNextEntry(new ZipEntry(resourceName));
                    zipStream.write(new byte[]{1, 2, 3}, 0, 3);
                }
            }
            classpathFiles = new ClasspathFiles(List.of(file));
        }

        @Test
        void getResourceNames() {
            assertEquals(List.of(resourceName), List.copyOf(classpathFiles.getResourceNames()));
        }

        @Test
        void openStream() throws IOException {
            try (val inputStream = classpathFiles.openStream(resourceName)) {
                assertNotNull(inputStream);
                assertArrayEquals(new byte[]{1, 2, 3}, toByteArray(inputStream));
            }
        }

    }

}
