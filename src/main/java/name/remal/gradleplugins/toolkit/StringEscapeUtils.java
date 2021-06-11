package name.remal.gradleplugins.toolkit;

public interface StringEscapeUtils {

    static String escapeJava(String string) {
        return escapeGroovy(string);
    }

    static String escapeGroovy(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(string);
    }

    static String escapeKotlin(String string) {
        return escapeJava(string)
            .replace("$", "\\$")
            ;
    }

    static String escapeJavaScript(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(string);
    }

    static String escapeRegex(String string) {
        return escapeJavaScript(string)
            .replace(".", "\\.")
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

    static String escapeJson(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeJson(string);
    }

    static String escapeHtml(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeHtml4(string);
    }

    static String escapeXml(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeXml10(string);
    }

    static String escapeCsv(String string) {
        return org.apache.commons.text.StringEscapeUtils.escapeCsv(string);
    }

}
