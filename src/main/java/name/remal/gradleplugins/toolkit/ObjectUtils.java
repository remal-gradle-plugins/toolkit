package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class ObjectUtils {

    @Contract("_->param1")
    public static <T> T doNotInline(T object) {
        return object;
    }


    @Contract("null -> true")
    public static boolean isEmpty(@Nullable CharSequence value) {
        return value == null || value.length() == 0;
    }

    @Contract("null -> true")
    public static boolean isEmpty(@Nullable Iterable<?> value) {
        return value == null || value.iterator().hasNext();
    }

    @Contract("null -> true")
    public static boolean isEmpty(@Nullable Collection<?> value) {
        return value == null || value.isEmpty();
    }

    @Contract("null -> true")
    public static boolean isEmpty(@Nullable Map<?, ?> value) {
        return value == null || value.isEmpty();
    }

    @Contract("null -> true")
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "java:S2789"})
    public static boolean isEmpty(@Nullable Optional<?> value) {
        return value == null || !value.isPresent();
    }


    @Contract("null -> false")
    public static boolean isNotEmpty(@Nullable CharSequence value) {
        return !isEmpty(value);
    }

    @Contract("null -> false")
    public static boolean isNotEmpty(@Nullable Iterable<?> value) {
        return !isEmpty(value);
    }

    @Contract("null -> false")
    public static boolean isNotEmpty(@Nullable Collection<?> value) {
        return !isEmpty(value);
    }

    @Contract("null -> false")
    public static boolean isNotEmpty(@Nullable Map<?, ?> value) {
        return !isEmpty(value);
    }

    @Contract("null -> false")
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "java:S2789"})
    public static boolean isNotEmpty(@Nullable Optional<?> value) {
        return !isEmpty(value);
    }

}
