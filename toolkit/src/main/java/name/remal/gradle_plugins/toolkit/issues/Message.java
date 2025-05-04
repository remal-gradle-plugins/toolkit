package name.remal.gradle_plugins.toolkit.issues;

import static lombok.AccessLevel.PROTECTED;
import static name.remal.gradle_plugins.toolkit.StringUtils.normalizeString;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.Language;

@NoArgsConstructor(access = PROTECTED, force = true)
public abstract class Message implements Serializable {

    @Language("TEXT")
    public abstract String renderAsText();


    @Getter
    @Nonnull
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
        var that = (Message) obj;
        return this.value.equals(that.value);
    }

    @Override
    public final int hashCode() {
        return this.value.hashCode();
    }

}
