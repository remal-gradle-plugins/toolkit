package name.remal.gradleplugins.toolkit;

import java.util.regex.Pattern;

public abstract class StringUtils {

    private static final Pattern TRIM_LINE_END = Pattern.compile("[ \\t]+(\\n|$)");
    private static final Pattern TOO_MANY_NEW_LINES = Pattern.compile("\\n{3,}");

    public static String normalizeString(String string) {
        if (string.isEmpty() || string.trim().isEmpty()) {
            return "";
        }

        string = string.replace("\r\n", "\n")
            .replace("\n\r", "\n")
            .replace("\r", "\n");

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

    private static final String INDENT = "  ";
    private static final Pattern INDENT_NEXT_LINE = Pattern.compile("\\n([^\\n])");

    public static String indentString(String string) {
        string = normalizeString(string);
        if (string.isEmpty()) {
            return "";
        }

        string = INDENT + INDENT_NEXT_LINE.matcher(string).replaceAll("\n" + INDENT + "$1");

        return string;
    }


    public static String escapeJava(String string) {
        return escapeGroovy(string);
    }

    public static String escapeGroovy(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(string);
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


    private StringUtils() {
    }

}
