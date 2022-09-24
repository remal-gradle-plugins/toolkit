package name.remal.gradleplugins.toolkit;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class ObjectUtils {

    @Contract(value = "_->param1", pure = true)
    public static <T> T doNotInline(T object) {
        return object;
    }


    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable CharSequence value) {
        return value == null || value.length() == 0;
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable Iterable<?> value) {
        return value == null || value.iterator().hasNext();
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable Collection<?> value) {
        return value == null || value.isEmpty();
    }

    @Contract(value = "null->true", pure = true)
    public static boolean isEmpty(@Nullable Map<?, ?> value) {
        return value == null || value.isEmpty();
    }

    @Contract(value = "null->true", pure = true)
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "java:S2789"})
    public static boolean isEmpty(@Nullable Optional<?> value) {
        return value == null || !value.isPresent();
    }


    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable CharSequence value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable Iterable<?> value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable Collection<?> value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    public static boolean isNotEmpty(@Nullable Map<?, ?> value) {
        return !isEmpty(value);
    }

    @Contract(value = "null->false", pure = true)
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "java:S2789"})
    public static boolean isNotEmpty(@Nullable Optional<?> value) {
        return !isEmpty(value);
    }


    @Contract(value = "!null,_->param1; null,_->param2", pure = true)
    public static <T> T defaultValue(@Nullable T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Contract(pure = true)
    public static String defaultValue(@Nullable String value) {
        return defaultValue(value, "");
    }

    @Contract(pure = true)
    public static <T> List<T> defaultValue(@Nullable List<T> value) {
        return defaultValue(value, emptyList());
    }

    @Contract(pure = true)
    public static <T> Set<T> defaultValue(@Nullable Set<T> value) {
        return defaultValue(value, emptySet());
    }

    @Contract(pure = true)
    public static <K, V> Map<K, V> defaultValue(@Nullable Map<K, V> value) {
        return defaultValue(value, emptyMap());
    }

    @Contract(pure = true)
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "java:S2789"})
    public static <T> Optional<T> defaultValue(@Nullable Optional<T> value) {
        return defaultValue(value, Optional.empty());
    }

}
