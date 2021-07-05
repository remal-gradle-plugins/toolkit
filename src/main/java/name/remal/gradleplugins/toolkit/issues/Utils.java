package name.remal.gradleplugins.toolkit.issues;

import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.jetbrains.annotations.Contract;

abstract class Utils {

    @Contract("null -> true")
    public static boolean isEmpty(@Nullable CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    @Contract("null -> false")
    public static boolean isNotEmpty(@Nullable CharSequence charSequence) {
        return !isEmpty(charSequence);
    }


    public static <T extends Comparable<T>> int compareOptionals(@Nullable T o1, @Nullable T o2) {
        if (o1 != null && o2 != null) {
            return o1.compareTo(o2);
        } else if (o1 != null) {
            return -1;
        } else if (o2 != null) {
            return 1;
        } else {
            return 0;
        }
    }

    public static Stream<Issue> streamIssues(Iterable<? extends Issue> issues) {
        return StreamSupport.stream(issues.spliterator(), false)
            .distinct()
            .sorted()
            .map(Issue.class::cast);
    }

    public static <T> void ifPresent(@Nullable T value, Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    @Contract("_,_ -> param1")
    public static StringBuilder appendDelimiter(StringBuilder sb, String delimiter) {
        if (sb.length() > 0) {
            sb.append(delimiter);
        }
        return sb;
    }

    @Contract("_ -> param1")
    public static StringBuilder appendDelimiter(StringBuilder sb) {
        appendDelimiter(sb, " ");
        return sb;
    }


    private Utils() {
    }

}
