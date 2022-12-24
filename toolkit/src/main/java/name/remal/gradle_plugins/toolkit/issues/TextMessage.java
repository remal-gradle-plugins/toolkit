package name.remal.gradle_plugins.toolkit.issues;

import org.intellij.lang.annotations.Language;

public class TextMessage extends Message {

    public static TextMessage textMessageOf(@Language("TEXT") String text) {
        return new TextMessage(text);
    }

    private TextMessage(String value) {
        super(value);
    }

    @Override
    public String renderAsText() {
        return this.value;
    }

}
