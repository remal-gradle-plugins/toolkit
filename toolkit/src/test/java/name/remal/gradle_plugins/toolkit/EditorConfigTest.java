package name.remal.gradle_plugins.toolkit;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.write;
import static name.remal.gradle_plugins.toolkit.JavaSerializationUtils.deserializeFrom;
import static name.remal.gradle_plugins.toolkit.JavaSerializationUtils.serializeToBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditorConfigTest {

    Path tempDir;
    EditorConfig editorConfig;

    @BeforeEach
    void beforeEach() throws Throwable {
        tempDir = createTempDirectory(EditorConfigTest.class.getSimpleName() + "-");
        editorConfig = new EditorConfig(tempDir);
    }

    @Test
    void noEditorConfigAndNoGitAttributes() {
        assertThat(editorConfig.getPropertiesForFileExtension("txt"))
            .isEmpty();
    }

    @Test
    void withEditorConfig() throws Throwable {
        write(tempDir.resolve(".editorconfig"), join(
            "\n",
            "[*]",
            "charset = utf-8",
            "end_of_line = lf"
        ).getBytes(UTF_8));
        assertThat(editorConfig.getPropertiesForFileExtension("txt"))
            .containsExactly(
                entry("charset", "utf-8"),
                entry("end_of_line", "lf")
            );
    }

    @Test
    void noEditorConfigAndWithGitAttributes() throws Throwable {
        write(tempDir.resolve(".gitattributes"), join(
            "\n",
            "*.txt working-tree-encoding=UTF-16 eol=CRLF"
        ).getBytes(UTF_8));
        assertThat(editorConfig.getPropertiesForFileExtension("txt"))
            .containsExactly(
                entry("charset", "utf-16be"),
                entry("end_of_line", "crlf")
            );
    }

    @Test
    void withEditorConfigAndWithGitAttributes() throws Throwable {
        write(tempDir.resolve(".editorconfig"), join(
            "\n",
            "[*]",
            "charset = utf-8"
        ).getBytes(UTF_8));
        write(tempDir.resolve(".gitattributes"), join(
            "\n",
            "*.txt working-tree-encoding=UTF-16 eol=CRLF"
        ).getBytes(UTF_8));
        assertThat(editorConfig.getPropertiesForFileExtension("txt"))
            .containsExactly(
                entry("charset", "utf-8"),
                entry("end_of_line", "crlf")
            );
    }

    @Test
    void serialization() throws Throwable {
        var bytes = serializeToBytes(editorConfig);
        var deserializedEditorConfig = deserializeFrom(bytes, EditorConfig.class);

        assertThat(deserializedEditorConfig)
            .isNotNull()
            .asString()
            .isEqualTo(editorConfig.toString());
    }

}
