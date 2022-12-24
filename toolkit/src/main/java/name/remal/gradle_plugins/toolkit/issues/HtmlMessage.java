package name.remal.gradle_plugins.toolkit.issues;

import static name.remal.gradle_plugins.toolkit.HtmlToTextUtils.convertHtmlToText;

import org.intellij.lang.annotations.Language;

public class HtmlMessage extends Message {

    public static HtmlMessage htmlMessageOf(@Language("HTML") String html) {
        return new HtmlMessage(html);
    }

    private HtmlMessage(String value) {
        super(value);
    }

    @Override
    public String renderAsText() {
        return convertHtmlToText(this.value);
    }

}
