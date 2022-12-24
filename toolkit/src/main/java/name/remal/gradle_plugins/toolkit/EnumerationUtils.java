package name.remal.gradle_plugins.toolkit;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

import com.google.common.collect.ImmutableList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class EnumerationUtils {

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <E> Enumeration<E> compoundEnumeration(Enumeration<E>... enumerations) {
        return new CompoundEnumeration<>(asList(enumerations));
    }

    public static <E> Enumeration<E> compoundEnumeration(Iterable<Enumeration<E>> enumerations) {
        return new CompoundEnumeration<>(enumerations);
    }

    @SuppressWarnings({"java:S1150", "JdkObsolete"})
    private static final class CompoundEnumeration<E> implements Enumeration<E> {

        private int index = 0;

        private final List<Enumeration<E>> enumerations;

        private CompoundEnumeration(Iterable<Enumeration<E>> enumerations) {
            this.enumerations = ImmutableList.copyOf(enumerations);
        }

        private boolean next() {
            for (; index < enumerations.size(); ++index) {
                val enumeration = enumerations.get(index);
                if (enumeration != null && enumeration.hasMoreElements()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean hasMoreElements() {
            return next();
        }

        @Override
        public E nextElement() {
            if (!next()) {
                throw new NoSuchElementException();
            } else {
                return enumerations.get(index).nextElement();
            }
        }

    }

}
