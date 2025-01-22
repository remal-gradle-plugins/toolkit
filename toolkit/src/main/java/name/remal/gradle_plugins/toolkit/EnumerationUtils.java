package name.remal.gradle_plugins.toolkit;

import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.StreamSupport;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class EnumerationUtils {

    @SafeVarargs
    @Contract(pure = true)
    @SuppressWarnings("varargs")
    public static <E> Enumeration<E> compoundEnumeration(Enumeration<E>... enumerations) {
        return new CompoundEnumeration<>(List.of(enumerations));
    }

    @Contract(pure = true)
    public static <E> Enumeration<E> compoundEnumeration(Iterable<Enumeration<E>> enumerations) {
        return new CompoundEnumeration<>(enumerations);
    }

    @SuppressWarnings({"java:S1150", "JdkObsolete"})
    private static final class CompoundEnumeration<E> implements Enumeration<E> {

        private int index = 0;

        private final List<Enumeration<E>> enumerations;

        private CompoundEnumeration(Iterable<Enumeration<E>> enumerations) {
            this.enumerations = StreamSupport.stream(enumerations.spliterator(), false)
                .filter(Objects::nonNull)
                .collect(toUnmodifiableList());
        }

        private boolean next() {
            for (; index < enumerations.size(); ++index) {
                var enumeration = enumerations.get(index);
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
