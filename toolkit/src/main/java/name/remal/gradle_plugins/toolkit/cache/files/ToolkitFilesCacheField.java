package name.remal.gradle_plugins.toolkit.cache.files;

import static java.lang.String.format;
import static java.lang.System.identityHashCode;

import java.util.regex.Pattern;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@Getter
@ToString
public abstract class ToolkitFilesCacheField<T> {

    private static final Pattern ALLOWED_ID_PATTERN = Pattern.compile("[a-z0-9_-]{2,25}");


    private final String id;

    private final Class<T> type;

    ToolkitFilesCacheField(String id, Class<T> type) {
        if (id.startsWith("sys-")) {
            throw new IllegalArgumentException(format(
                "ID must not start with 'sys-': %s",
                id
            ));
        }
        if (!ALLOWED_ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException(format(
                "ID must match pattern %s: %s",
                ALLOWED_ID_PATTERN.pattern(),
                id
            ));
        }
        this.id = id;
        this.type = type;
    }


    abstract byte @Nullable [] serialize(T value);

    abstract T deserialize(byte[] bytes);


    @Override
    public final boolean equals(@Nullable Object other) {
        return this == other;
    }

    @Override
    public final int hashCode() {
        return identityHashCode(this);
    }

}
