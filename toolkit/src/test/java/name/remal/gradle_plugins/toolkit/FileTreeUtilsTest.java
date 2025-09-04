package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.writeString;
import static name.remal.gradle_plugins.toolkit.ArchiveUtils.newZipArchiveWriter;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.deleteRecursively;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class FileTreeUtilsTest {

    private final Project project;

    @Test
    void getFileTreeRoots() throws Throwable {
        var zipFile = normalizeFile(project.file("archive.zip"));
        try (var archiveWriter = newZipArchiveWriter(zipFile)) {
            archiveWriter.writeEntry("archive-file.txt", "text");
        }

        var dir = normalizeFile(project.file("dir"));
        writeString(createParentDirectories(dir.toPath().resolve("dir-file.txt")), "text");

        var notExistingFile = normalizeFile(project.file("not-existing"));
        deleteRecursively(notExistingFile.toPath());


        var zipTree = project.zipTree(zipFile);
        assertThat(FileTreeUtils.getFileTreeRoots(zipTree))
            .containsExactlyInAnyOrder(zipFile);

        var dirTree = project.fileTree(dir);
        assertThat(FileTreeUtils.getFileTreeRoots(dirTree))
            .containsExactlyInAnyOrder(dir);

        var notExistingTree = project.fileTree(notExistingFile);
        assertThat(FileTreeUtils.getFileTreeRoots(notExistingTree))
            .containsExactlyInAnyOrder(notExistingFile);

        var compositeTree = zipTree.plus(dirTree).plus(notExistingTree);
        assertThat(FileTreeUtils.getFileTreeRoots(compositeTree))
            .containsExactlyInAnyOrder(zipFile, dir, notExistingFile);
    }

}
