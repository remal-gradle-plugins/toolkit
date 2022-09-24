package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.StringUtils.escapeHtml;
import static name.remal.gradleplugins.toolkit.StringUtils.normalizeString;

import lombok.NoArgsConstructor;
import lombok.val;
import net.htmlparser.jericho.Source;
import org.intellij.lang.annotations.Language;

@NoArgsConstructor(access = PRIVATE)
public abstract class HtmlToTextUtils {

    @Language("TEXT")
    @SuppressWarnings("java:S109")
    public static String convertHtmlToText(@Language("HTML") String html) {
        val text = new Source(html).getRenderer()
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
    public static String convertTextToHtml(@Language("TEXT") String text) {
        text = normalizeString(text);
        return escapeHtml(text)
            .replace("\n", "<br/>");
    }

}
