package name.remal.gradle_plugins.toolkit.issues;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.HtmlToTextUtils.convertHtmlToText;

import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.Language;

@NoArgsConstructor(access = PRIVATE, force = true)
public final class HtmlMessage extends Message {

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


    private static final long serialVersionUID = 1;

}
