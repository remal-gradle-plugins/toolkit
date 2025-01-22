package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.StringUtils.escapeHtml;
import static name.remal.gradle_plugins.toolkit.StringUtils.normalizeString;

import lombok.NoArgsConstructor;
import net.htmlparser.jericho.Source;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class HtmlToTextUtils {

    @Language("TEXT")
    @Contract(pure = true)
    @SuppressWarnings("java:S109")
    public static String convertHtmlToText(@Language("HTML") String html) {
        var text = new Source(html).getRenderer()
            .setMaxLineLength(Integer.MAX_VALUE)
            .setHRLineLength(20)
            .setConvertNonBreakingSpaces(true)
            .setIncludeFirstElementTopMargin(false)
            .setIncludeHyperlinkURLs(true)
            .setIncludeAlternateText(true)
            .setNewLine("\n")
            .setBlockIndentSize(2)
            .setListIndentSize(2)
            .setTableCellSeparator(" | ")
            .toString();
        return normalizeString(text);
    }

    @Language("HTML")
    @Contract(pure = true)
    public static String convertTextToHtml(@Language("TEXT") String text) {
        text = normalizeString(text);
        return escapeHtml(text)
            .replace("\n", "<br/>");
    }

}
