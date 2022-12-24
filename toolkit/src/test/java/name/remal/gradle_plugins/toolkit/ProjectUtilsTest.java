package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.write;
import static name.remal.gradle_plugins.toolkit.ArchiveUtils.newZipArchiveWriter;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileVisitDetails;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ProjectUtilsTest {

    private final Project project;

    @Test
    void newClasspathFileTree() throws Throwable {
        val projectDir = normalizePath(project.getProjectDir().toPath());

        val fileA = projectDir.resolve("dir-a/a-file");
        write(createParentDirectories(fileA), new byte[]{1});

        val fileB = projectDir.resolve("dir-b/b-file");
        write(createParentDirectories(fileB), new byte[]{2});

        val archive = projectDir.resolve("archive.zip");
        try (val archiveWriter = newZipArchiveWriter(archive)) {
            archiveWriter.writeEntry("entry", new byte[]{3});
        }

        val fileTree = ProjectUtils.newClasspathFileTree(project, List.of(
            fileA.getParent().toFile(),
            fileB.getParent().toFile(),
            archive.toFile()
        ));

        List<String> relativePaths = new ArrayList<>();
        fileTree.visit(new EmptyFileVisitor() {
            @Override
            public void visitFile(FileVisitDetails details) {
                relativePaths.add(details.getPath());
            }
        });
        assertThat(relativePaths).containsExactlyInAnyOrder(
            "a-file",
            "b-file",
            "entry"
        );
    }

}
