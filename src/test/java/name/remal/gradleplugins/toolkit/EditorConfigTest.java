package name.remal.gradleplugins.toolkit;

import static java.lang.String.join;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.writeString;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditorConfigTest {

    Path tempDir;
    EditorConfig editorConfig;

    @BeforeEach
    void beforeEach() throws Throwable {
        tempDir = createTempDirectory(EditorConfigTest.class.getSimpleName() + "-'");
        editorConfig = new EditorConfig(tempDir);
    }

    @Test
    void noEditorConfigAndNoGitAttributes() {
        assertThat(editorConfig.getPropertiesForFileExtension("txt"))
            .isEmpty();
    }

    @Test
    void withEditorConfig() throws Throwable {
        writeString(tempDir.resolve(".editorconfig"), join(
            "\n",
            "[*]",
            "charset = utf-8",
            "end_of_line = lf"
        ));
        assertThat(editorConfig.getPropertiesForFileExtension("txt"))
            .containsExactly(
                entry("charset", "utf-8"),
                entry("end_of_line", "lf")
            );
    }

    @Test
    void noEditorConfigAndWithGitAttributes() throws Throwable {
        writeString(tempDir.resolve(".gitattributes"), join(
            "\n",
            "*.txt working-tree-encoding=UTF-16 eol=CRLF"
        ));
        assertThat(editorConfig.getPropertiesForFileExtension("txt"))
            .containsExactly(
                entry("charset", "utf-16be"),
                entry("end_of_line", "crlf")
            );
    }

    @Test
    void withEditorConfigAndWithGitAttributes() throws Throwable {
        writeString(tempDir.resolve(".editorconfig"), join(
            "\n",
            "[*]",
            "charset = utf-8"
        ));
        writeString(tempDir.resolve(".gitattributes"), join(
            "\n",
            "*.txt working-tree-encoding=UTF-16 eol=CRLF"
        ));
        assertThat(editorConfig.getPropertiesForFileExtension("txt"))
            .containsExactly(
                entry("charset", "utf-8"),
                entry("end_of_line", "crlf")
            );
    }

}
