package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.util.function.Predicate;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class PredicateUtils {

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> not(Predicate<? super T> target) {
        return (Predicate<T>) target.negate();
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

}
