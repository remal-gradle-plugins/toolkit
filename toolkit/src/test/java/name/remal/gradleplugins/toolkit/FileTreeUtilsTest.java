package name.remal.gradleplugins.toolkit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.write;
import static name.remal.gradleplugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradleplugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradleplugins.toolkit.PathUtils.deleteRecursively;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class FileTreeUtilsTest {

    private final Project project;

    @Test
    void getFileTreeRoots() throws Throwable {
        val zipFile = normalizeFile(project.file("archive.zip"));
        try (val outputStream = newOutputStream(createParentDirectories(zipFile.toPath()))) {
            try (val zipOutputStream = new ZipOutputStream(outputStream, UTF_8)) {
                zipOutputStream.putNextEntry(new ZipEntry("archive-file.txt"));
                zipOutputStream.write("text".getBytes(UTF_8));
            }
        }

        val dir = normalizeFile(project.file("dir"));
        write(createParentDirectories(dir.toPath().resolve("dir-file.txt")), "text".getBytes(UTF_8));

        val notExistingFile = normalizeFile(project.file("not-existing"));
        deleteRecursively(notExistingFile.toPath());


        val zipTree = project.zipTree(zipFile);
        assertThat(FileTreeUtils.getFileTreeRoots(zipTree))
            .containsExactlyInAnyOrder(zipFile);

        val dirTree = project.fileTree(dir);
        assertThat(FileTreeUtils.getFileTreeRoots(dirTree))
            .containsExactlyInAnyOrder(dir);

        val notExistingTree = project.fileTree(notExistingFile);
        assertThat(FileTreeUtils.getFileTreeRoots(notExistingTree))
            .containsExactlyInAnyOrder(notExistingFile);

        val compositeTree = zipTree.plus(dirTree).plus(notExistingTree);
        assertThat(FileTreeUtils.getFileTreeRoots(compositeTree))
            .containsExactlyInAnyOrder(zipFile, dir, notExistingFile);
    }

}
