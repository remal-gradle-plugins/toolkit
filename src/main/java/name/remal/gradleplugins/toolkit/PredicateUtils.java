package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

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

    public static <T> Predicate<T> notEqualsTo(@Nullable T value) {
        return not(equalsTo(value));
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

    public static <T extends CharSequence> Predicate<T> notStartsWithString(CharSequence value) {
        return not(startsWithString(value));
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

    public static <T extends CharSequence> Predicate<T> notEndsWithString(CharSequence value) {
        return not(endsWithString(value));
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

    public static <T extends CharSequence> Predicate<T> notContainsString(CharSequence value) {
        return not(containsString(value));
    }

}
