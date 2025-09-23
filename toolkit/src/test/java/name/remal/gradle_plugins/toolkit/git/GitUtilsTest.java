package name.remal.gradle_plugins.toolkit.git;

import static java.lang.String.join;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.writeString;
import static name.remal.gradle_plugins.toolkit.git.GitBooleanAttribute.newGitBooleanAttributeBuilder;
import static name.remal.gradle_plugins.toolkit.git.GitStringAttribute.newGitStringAttributeBuilder;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("StringJoin")
class GitUtilsTest {

    Path repositoryRoot;

    @BeforeEach
    void beforeEach() throws Throwable {
        repositoryRoot = createTempDirectory(GitUtilsTest.class.getSimpleName() + '-');
    }

    @Test
    void noFiles() {
        assertThat(GitUtils.getGitAttributesFor(repositoryRoot, "file.txt"))
            .isEmpty();
    }

    @Test
    void noGitAttributesFile() throws Throwable {
        createFile(repositoryRoot.resolve("file.txt"));
        assertThat(GitUtils.getGitAttributesFor(repositoryRoot, "file.txt"))
            .isEmpty();
    }

    @Test
    void emptyGitAttributesFile() throws Throwable {
        createFile(repositoryRoot.resolve("file.txt"));
        createFile(repositoryRoot.resolve(".gitattributes"));
        assertThat(GitUtils.getGitAttributesFor(repositoryRoot, "file.txt"))
            .isEmpty();
    }

    @Test
    void withGitAttributesFileEntry() throws Throwable {
        createFile(repositoryRoot.resolve("file.txt"));
        writeString(repositoryRoot.resolve(".gitattributes"), join(
            "\n",
            "*.txt -text eol=crlf"
        ));
        assertThat(GitUtils.getGitAttributesFor(repositoryRoot, "file.txt"))
            .containsExactlyInAnyOrder(
                newGitBooleanAttributeBuilder()
                    .name("text")
                    .set(false)
                    .build(),
                newGitStringAttributeBuilder()
                    .name("eol")
                    .value("crlf")
                    .build()
            );
    }

}
