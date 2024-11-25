package name.remal.gradle_plugins.toolkit.testkit.internal.containers;

import static java.lang.Character.MAX_CODE_POINT;
import static java.lang.Character.MIN_CODE_POINT;
import static java.lang.Character.toChars;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.Test;

class ProjectDirPrefixTest {

    @Test
    void charsAreProperlyEscapedForTempDirPrefix() {
        val string = new StringBuilder();
        IntStream.rangeClosed(MIN_CODE_POINT, MAX_CODE_POINT).forEach(codePoint -> {
            for (val ch : toChars(codePoint)) {
                string.append(ch);
            }
        });
        string.append("a");

        val dirPrefix = new ProjectDirPrefix().push(string.toString());
        val prefixString = dirPrefix.getTempDirPrefix();
        assertThat(prefixString)
            .hasSize(string.length() + 1)
            .endsWith("-")
            .doesNotContain(
                "\\",
                "/",
                ":",
                "<",
                ">",
                "\"",
                "'",
                "|",
                "?",
                "*",
                "$",
                "{",
                "}",
                "'",
                "(",
                ")",
                "&",
                "[",
                "]",
                "^"
            );

        IntStream.rangeClosed(MIN_CODE_POINT, 31).forEach(codePoint -> {
            for (val ch : toChars(codePoint)) {
                assertThat(prefixString).doesNotContain(String.valueOf(ch));
            }
        });

        IntStream.rangeClosed(127, MAX_CODE_POINT).forEach(codePoint -> {
            for (val ch : toChars(codePoint)) {
                assertThat(prefixString).doesNotContain(String.valueOf(ch));
            }
        });
    }

}
