package name.remal.gradleplugins.toolkit.issues;

import static name.remal.gradleplugins.toolkit.StringUtils.normalizeString;

import javax.annotation.Nullable;
import lombok.val;
import org.intellij.lang.annotations.Language;

public abstract class Message {

    @Language("TEXT")
    public abstract String renderAsText();


    protected final String value;

    protected Message(String value) {
        this.value = normalizeString(value);
    }

    @Override
    public final String toString() {
        return this.value;
    }

    @Override
    @SuppressWarnings("EqualsGetClass")
    public final boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        val that = (Message) obj;
        return this.value.equals(that.value);
    }

    @Override
    public final int hashCode() {
        return this.value.hashCode();
    }

}
