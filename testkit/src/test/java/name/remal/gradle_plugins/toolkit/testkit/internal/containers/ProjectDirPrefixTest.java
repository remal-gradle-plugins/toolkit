package name.remal.gradle_plugins.toolkit.testkit.internal.containers;

import static java.lang.Character.MAX_CODE_POINT;
import static java.lang.Character.MIN_CODE_POINT;
import static java.lang.Character.toChars;
import static java.lang.Math.min;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ProjectDirPrefixTest {

    @Test
    void charsAreProperlyEscapedForTempDirPrefix() {
        var minCodePoint = MIN_CODE_POINT;
        var maxCodePoint = min(MAX_CODE_POINT, 2000);

        var string = new StringBuilder();
        IntStream.rangeClosed(minCodePoint, maxCodePoint).forEach(codePoint -> {
            for (var ch : toChars(codePoint)) {
                string.append(ch);
            }
        });
        string.append("a");

        var dirPrefix = new ProjectDirPrefix().push(string.toString());
        var prefixString = dirPrefix.getTempDirPrefix();
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

        IntStream.rangeClosed(minCodePoint, 31).forEach(codePoint -> {
            for (var ch : toChars(codePoint)) {
                assertThat(prefixString).doesNotContain(String.valueOf(ch));
            }
        });

        IntStream.rangeClosed(127, maxCodePoint).forEach(codePoint -> {
            for (var ch : toChars(codePoint)) {
                assertThat(prefixString).doesNotContain(String.valueOf(ch));
            }
        });
    }

}
