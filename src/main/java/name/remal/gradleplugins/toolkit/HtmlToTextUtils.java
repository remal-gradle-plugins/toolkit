package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.StringUtils.escapeHtml;
import static name.remal.gradleplugins.toolkit.StringUtils.normalizeString;

import lombok.val;
import net.htmlparser.jericho.Source;
import org.intellij.lang.annotations.Language;

public interface HtmlToTextUtils {

    @Language("TEXT")
    @SuppressWarnings("java:S109")
    static String convertHtmlToText(@Language("HTML") String html) {
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
    static String convertTextToHtml(@Language("TEXT") String text) {
        text = normalizeString(text);
        return escapeHtml(text)
            .replace("\n", "<br>")
            ;
    }

}
