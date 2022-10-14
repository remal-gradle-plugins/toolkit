package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class StringUtils {

    private static final Pattern NEW_LINE = Pattern.compile("\\r\\n|\\n\\r|\\r|\\n");
    private static final Pattern TRIM_LINE_END = Pattern.compile("[ \\t]+(?:\\n|$)");
    private static final Pattern TOO_MANY_NEW_LINES = Pattern.compile("\\n{3,}");

    public static String normalizeNewLines(String string) {
        return NEW_LINE.matcher(string).replaceAll("\n");
    }

    public static String normalizeString(String string) {
        if (string.isEmpty() || string.trim().isEmpty()) {
            return "";
        }

        string = normalizeNewLines(string);

        string = TRIM_LINE_END.matcher(string).replaceAll("\n");
        string = TOO_MANY_NEW_LINES.matcher(string).replaceAll("\n\n");

        while (string.startsWith("\n")) {
            string = string.substring(1);
        }
        while (string.endsWith("\n")) {
            string = string.substring(0, string.length() - 1);
        }

        return string;
    }

    private static final Pattern INDENT_NEXT_LINE = Pattern.compile("\\n([^\\n])");

    public static String indentString(String string) {
        return indentString(string, 2);
    }

    public static String indentString(String string, int indentSize) {
        string = normalizeString(string);
        if (string.isEmpty()) {
            return "";
        }

        if (indentSize <= 0) {
            return string;
        }

        val indentBuilder = new StringBuilder(indentSize);
        for (int n = 1; n <= indentSize; ++n) {
            indentBuilder.append(' ');
        }
        val indent = indentBuilder.toString();

        string = indent + INDENT_NEXT_LINE.matcher(string).replaceAll("\n" + indent + "$1");

        return string;
    }


    public static String trimWith(String string, CharPredicate charPredicate) {
        if (string.isEmpty()) {
            return "";
        } else if (charPredicate == ALWAYS_FALSE_CHAR_PREDICATE) {
            return string;
        }

        val startPos = calculateTrimmedStartPos(string, charPredicate);
        if (startPos >= string.length()) {
            return "";
        }

        val endPos = calculateTrimmedEndPos(string, charPredicate);

        return string.substring(startPos, endPos + 1);
    }

    public static String trimWith(String string, char... charsToRemove) {
        return trimWith(string, charPredicateOf(charsToRemove));
    }

    public static String trimWith(String string, CharSequence charsToRemove) {
        return trimWith(string, charPredicateOf(charsToRemove));
    }

    public static String trimLeftWith(String string, CharPredicate charPredicate) {
        if (string.isEmpty()) {
            return "";
        } else if (charPredicate == ALWAYS_FALSE_CHAR_PREDICATE) {
            return string;
        }

        val startPos = calculateTrimmedStartPos(string, charPredicate);
        if (startPos >= string.length()) {
            return "";
        }

        val endPos = string.length() - 1;

        return string.substring(startPos, endPos + 1);
    }

    public static String trimLeftWith(String string, char... charsToRemove) {
        return trimLeftWith(string, charPredicateOf(charsToRemove));
    }

    public static String trimLeftWith(String string, CharSequence charsToRemove) {
        return trimLeftWith(string, charPredicateOf(charsToRemove));
    }

    public static String trimLeft(String string) {
        return trimLeftWith(string, Character::isWhitespace);
    }

    public static String trimRightWith(String string, CharPredicate charPredicate) {
        if (string.isEmpty()) {
            return "";
        } else if (charPredicate == ALWAYS_FALSE_CHAR_PREDICATE) {
            return string;
        }

        val startPos = 0;

        val endPos = calculateTrimmedEndPos(string, charPredicate);
        if (endPos < 0) {
            return "";
        }

        return string.substring(startPos, endPos + 1);
    }

    public static String trimRightWith(String string, char... charsToRemove) {
        return trimRightWith(string, charPredicateOf(charsToRemove));
    }

    public static String trimRightWith(String string, CharSequence charsToRemove) {
        return trimRightWith(string, charPredicateOf(charsToRemove));
    }

    public static String trimRight(String string) {
        return trimRightWith(string, Character::isWhitespace);
    }

    @SneakyThrows
    private static int calculateTrimmedStartPos(String string, CharPredicate charPredicate) {
        int startPos = 0;
        for (; startPos < string.length(); ++startPos) {
            val ch = string.charAt(startPos);
            if (!charPredicate.test(ch)) {
                break;
            }
        }
        return startPos;
    }

    @SneakyThrows
    private static int calculateTrimmedEndPos(String string, CharPredicate charPredicate) {
        int endPos = string.length() - 1;
        for (; 0 <= endPos; --endPos) {
            val ch = string.charAt(endPos);
            if (!charPredicate.test(ch)) {
                break;
            }
        }
        return endPos;
    }

    private static CharPredicate charPredicateOf(char... charsToRemove) {
        if (charsToRemove.length == 0) {
            return ALWAYS_FALSE_CHAR_PREDICATE;
        }

        return ch -> {
            for (val charToRemove : charsToRemove) {
                if (ch == charToRemove) {
                    return true;
                }
            }
            return false;
        };
    }

    private static CharPredicate charPredicateOf(CharSequence charsToRemove) {
        if (charsToRemove.length() == 0) {
            return ALWAYS_FALSE_CHAR_PREDICATE;
        }

        return ch -> {
            for (int i = 0; i < charsToRemove.length(); ++i) {
                val charToRemove = charsToRemove.charAt(i);
                if (ch == charToRemove) {
                    return true;
                }
            }
            return false;
        };
    }

    @FunctionalInterface
    public interface CharPredicate {
        boolean test(char ch) throws Throwable;
    }

    private static final CharPredicate ALWAYS_FALSE_CHAR_PREDICATE = __ -> false;


    public static String escapeJava(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeJava(string);
    }

    public static String escapeGroovy(String string) {
        return escapeJava(string)
            .replace("$", "\\$")
            ;
    }

    public static String escapeKotlin(String string) {
        return escapeJava(string)
            .replace("$", "\\$")
            ;
    }

    public static String escapeJavaScript(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(string);
    }

    public static String escapeRegex(String string) {
        return escapeJavaScript(string)
            .replace(".", "\\.")
            .replace("|", "\\|")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("?", "\\?")
            .replace("*", "\\*")
            .replace("+", "\\+")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("^", "\\^")
            .replace("$", "\\$")
            ;
    }

    public static String escapeJson(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeJson(string);
    }

    public static String escapeHtml(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeHtml4(string);
    }

    public static String escapeXml(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeXml10(string);
    }

    public static String escapeCsv(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeCsv(string);
    }

}
