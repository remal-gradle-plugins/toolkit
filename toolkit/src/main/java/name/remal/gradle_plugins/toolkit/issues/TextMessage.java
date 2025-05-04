package name.remal.gradle_plugins.toolkit.issues;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.Language;

@NoArgsConstructor(access = PRIVATE, force = true)
public final class TextMessage extends Message {

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
