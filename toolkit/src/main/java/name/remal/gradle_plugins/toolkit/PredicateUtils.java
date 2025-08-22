package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class PredicateUtils {

    @Contract(pure = true)
    public static <T> Predicate<T> equalsTo(@Nullable T value) {
        return object -> Objects.equals(object, value);
    }


    @Contract(pure = true)
    public static <T extends CharSequence> Predicate<T> startsWithString(CharSequence value) {
        return charSequence -> {
            if (charSequence == null) {
                return false;
            }

            var string = charSequence.toString();
            return string.startsWith(value.toString());
        };
    }

    @Contract(pure = true)
    public static <T extends CharSequence> Predicate<T> endsWithString(CharSequence value) {
        return charSequence -> {
            if (charSequence == null) {
                return false;
            }

            var string = charSequence.toString();
            return string.endsWith(value.toString());
        };
    }

    @Contract(pure = true)
    public static <T extends CharSequence> Predicate<T> containsString(CharSequence value) {
        return charSequence -> {
            if (charSequence == null) {
                return false;
            }

            var string = charSequence.toString();
            return string.contains(value);
        };
    }


    @Contract(pure = true)
    public static Predicate<Path> startsWithPath(Path value) {
        return path -> {
            if (path == null) {
                return false;
            }

            return path.startsWith(value);
        };
    }

    @Contract(pure = true)
    public static Predicate<Path> endsWithPath(Path value) {
        return path -> {
            if (path == null) {
                return false;
            }

            return path.endsWith(value);
        };
    }

}
