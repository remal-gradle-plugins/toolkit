package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class PredicateUtils {

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> not(Predicate<? super T> target) {
        return (Predicate<T>) target.negate();
    }


    public static <T> Predicate<T> equalsTo(@Nullable T value) {
        return object -> Objects.equals(object, value);
    }


    public static <T extends CharSequence> Predicate<T> startsWithString(CharSequence value) {
        return charSequence -> {
            if (charSequence == null) {
                return false;
            }

            val string = charSequence.toString();
            return string.startsWith(value.toString());
        };
    }

    public static <T extends CharSequence> Predicate<T> endsWithString(CharSequence value) {
        return charSequence -> {
            if (charSequence == null) {
                return false;
            }

            val string = charSequence.toString();
            return string.endsWith(value.toString());
        };
    }

    public static <T extends CharSequence> Predicate<T> containsString(CharSequence value) {
        return charSequence -> {
            if (charSequence == null) {
                return false;
            }

            val string = charSequence.toString();
            return string.contains(value);
        };
    }


    public static Predicate<Path> startsWithPath(Path value) {
        return path -> {
            if (path == null) {
                return false;
            }

            return path.startsWith(value);
        };
    }

    public static Predicate<Path> endsWithPath(Path value) {
        return path -> {
            if (path == null) {
                return false;
            }

            return path.endsWith(value);
        };
    }

}
