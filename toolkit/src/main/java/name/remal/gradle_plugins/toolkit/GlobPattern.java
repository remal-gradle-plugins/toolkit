package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.sort;
import static lombok.AccessLevel.PRIVATE;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.file.RelativePath;
import org.jetbrains.annotations.Contract;

@RequiredArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(of = "pattern")
public final class GlobPattern {

    private static final byte STAR_MODE_START_OF_PART_MASK = 1;
    private static final byte STAR_MODE_MULTIPLE_MASK = STAR_MODE_START_OF_PART_MASK << 1;
    private static final byte STAR_MODE_MULTIPLE_WITH_SLASH_MASK = STAR_MODE_MULTIPLE_MASK << 1;

    @Contract(pure = true)
    @SuppressWarnings({"java:S127", "java:S3776", "java:S6541"})
    public static GlobPattern compile(String pattern) {
        // trailing / or \ assumes **
        if (pattern.endsWith("/") || pattern.endsWith("\\")) {
            pattern += "**";
        }

        val regex = new StringBuilder();
        regex.append('^');
        for (int index = 0; index < pattern.length(); index++) {
            val ch = pattern.charAt(index);
            if (binarySearch(CHARS_TO_ESCAPE, ch) >= 0) {
                regex.append('\\').append(ch);

            } else if (ch == '*') {
                byte starMode = 0;
                if (index == 0) {
                    starMode |= STAR_MODE_START_OF_PART_MASK;
                } else {
                    val prevCh = pattern.charAt(index - 1);
                    if (prevCh == '/' || prevCh == '\\') {
                        starMode |= STAR_MODE_START_OF_PART_MASK;
                    }
                }

                while (index < pattern.length() - 1) {
                    val nextCh = pattern.charAt(index + 1);
                    if ((starMode & STAR_MODE_MULTIPLE_WITH_SLASH_MASK) == 0
                        && nextCh == '*'
                    ) {
                        starMode |= STAR_MODE_MULTIPLE_MASK;
                        ++index;
                        continue;
                    }
                    if ((starMode & STAR_MODE_START_OF_PART_MASK) != 0
                        && (starMode & STAR_MODE_MULTIPLE_MASK) != 0
                        && (nextCh == '/' || nextCh == '\\')
                    ) {
                        starMode |= STAR_MODE_MULTIPLE_WITH_SLASH_MASK;
                        ++index;
                        continue;
                    }
                    break;
                }
                if ((starMode & STAR_MODE_MULTIPLE_MASK) == 0) {
                    regex.append("[^/\\\\]*");
                } else if ((starMode & STAR_MODE_MULTIPLE_WITH_SLASH_MASK) != 0) {
                    regex.append("(?:.+[/\\\\]+)?");
                } else {
                    regex.append(".*");
                }

            } else if (ch == '/' || ch == '\\') {
                regex.append("[/\\\\]+");

                while (index < pattern.length() - 1) {
                    val nextCh = pattern.charAt(index + 1);
                    if (nextCh == '/' || nextCh == '\\') {
                        ++index;
                    } else {
                        break;
                    }
                }

            } else if (ch == '\t') {
                regex.append("\\t");
            } else if (ch == '\n') {
                regex.append("\\n");
            } else if (ch == '\r') {
                regex.append("\\r");
            } else if (ch == '\f') {
                regex.append("\\f");

            } else if (ch <= 31 || 127 <= ch) {
                regex.append("\\u").append(format("%04x", (int) ch).toUpperCase());

            } else {
                regex.append(ch);
            }
        }
        regex.append('$');

        return new GlobPattern(pattern, Pattern.compile(regex.toString()));
    }

    private static final char[] CHARS_TO_ESCAPE = ".|[]()?+{}^$".toCharArray();

    static {
        sort(CHARS_TO_ESCAPE);
    }


    private final String pattern;
    private final Pattern regex;

    public boolean matches(String path) {
        return regex.matcher(path).matches();
    }

    public boolean matches(RelativePath relativePath) {
        return matches(relativePath.getPathString());
    }

    @Override
    public String toString() {
        return pattern;
    }

}
