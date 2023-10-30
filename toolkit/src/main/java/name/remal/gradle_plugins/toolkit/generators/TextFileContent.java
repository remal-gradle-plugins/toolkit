package name.remal.gradle_plugins.toolkit.generators;

import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.unwrapProviders;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.Contract;

public class TextFileContent<
    Child extends TextFileContent<Child>
    > implements TextFileContentChunk {

    @Contract("->this")
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    protected final Child getSelf() {
        return (Child) this;
    }


    protected final List<Object> chunks = new ArrayList<>();

    public final String getContent() {
        val sb = new StringBuilder();
        appendToContent(chunks, sb);
        return sb.toString();
    }

    private void appendToContent(@Nullable Object object, StringBuilder content) {
        object = unwrapProviders(object);
        if (object == null) {
            return;
        }

        if (object instanceof TextFileContentChunk) {
            appendLineToContent(object, content);

        } else if (object instanceof Iterable<?>) {
            for (val element : (Iterable<?>) object) {
                appendToContent(element, content);
            }

        } else if (object.getClass().isArray()) {
            val length = Array.getLength(object);
            for (int i = 0; i < length; ++i) {
                val element = Array.get(object, i);
                appendToContent(element, content);
            }

        } else {
            appendLineToContent(object, content);
        }
    }

    private void appendLineToContent(Object object, StringBuilder content) {
        if (isNotEmpty(content)) {
            content.append('\n');
        }
        content.append(object);
    }


    @Contract("_->this")
    @CanIgnoreReturnValue
    public final Child append(@Nullable Object... chunks) {
        this.chunks.add(chunks);
        return getSelf();
    }


    @Override
    public String toString() {
        return getContent();
    }

}
