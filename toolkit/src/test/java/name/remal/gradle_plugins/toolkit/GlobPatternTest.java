package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMap;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

class GlobPatternTest {

    @Test
    @SuppressWarnings({"UnnecessaryUnicodeEscape", "AvoidEscapedUnicodeCharacters"})
    void scenarios() {
        ImmutableMap.<Pair<String, String>, Boolean>builder()
            .put(Pair.of("dir/file", "dir/file"), true)
            .put(Pair.of("dir/file", "dir/file/file"), false)
            .put(Pair.of("dir/file", "dir//file"), true)
            .put(Pair.of("dir/file", "dir//\\\\file"), true)
            .put(Pair.of("dir//file", "dir//\\\\file"), true)
            .put(Pair.of("dir//\\file", "dir//\\\\file"), true)
            .put(Pair.of("dir/file", "dir\\file"), true)
            .put(Pair.of("dir/file", "dir\\\\file"), true)
            .put(Pair.of("dir\\file", "dir/file"), true)
            .put(Pair.of("dir\\file", "dir\\file"), true)
            .put(Pair.of("dir/", "dir/file"), true)
            .put(Pair.of("dir/", "dir/dir/file"), true)
            .put(Pair.of("dir/**", "dir/file"), true)
            .put(Pair.of("dir/**", "dir/dir/file"), true)
            .put(Pair.of("dir/**", "file/dir/file"), false)
            .put(Pair.of("dir/*", "dir/file"), true)
            .put(Pair.of("dir/*", "dir/dir/file"), false)
            .put(Pair.of("dir/dir-*/file", "dir/dir/file"), false)
            .put(Pair.of("dir/dir-*/file", "dir/dir-/file"), true)
            .put(Pair.of("dir/dir-*/file", "dir/dir-asd/file"), true)
            .put(Pair.of("dir/dir-*/file", "dir/dir-asd/dir/file"), false)
            .put(Pair.of("dir/dir-**/file", "dir/dir-asd/dir/file"), true)
            .put(Pair.of("dir/dir-**//file", "dir/dir-asd/dir/file"), true)
            .put(Pair.of("*.java", "class.java"), true)
            .put(Pair.of("*.java", "package/class.java"), false)
            .put(Pair.of("**/*.java", "class.java"), true)
            .put(Pair.of("**/*.java", "package/class.java"), true)
            .put(Pair.of("**/*.java", "package/package/class.java"), true)
            .put(Pair.of("dir/fi\nle", "dir/fi\nle"), true)
            .put(Pair.of("dir/fi.le", "dir/fi.le"), true)
            .put(Pair.of("dir/fi.le", "dir/fi-le"), false)
            .put(Pair.of("dir/fi\u01F4le", "dir/fi\u01F4le"), true)
            .put(Pair.of("dir/fi\u0002le", "dir/fi\u0002le"), true)
            .put(Pair.of("dir/**/file", "dir/dir/file"), true)
            .put(Pair.of("dir/**/file", "dir/dir/dir/file"), true)
            .put(Pair.of("dir/**//file", "dir/dir/dir/file"), true)
            .put(Pair.of("dir/**/file", "dir/file"), true)
            .put(Pair.of("dir/**//file", "dir/file"), true)
            .build()
            .forEach((pair, expectedResult) -> {
                val globPattern = GlobPattern.compile(pair.getLeft());
                val path = pair.getRight();
                assertEquals(
                    expectedResult,
                    globPattern.matches(path),
                    format("Glob pattern `%s` does NOT match path `%s`", pair.getLeft(), path)
                );
            });
    }

}
