package name.remal.gradle_plugins.toolkit;

import static java.lang.Character.isDigit;
import static java.lang.Math.min;
import static lombok.AccessLevel.PRIVATE;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public class NumbersAwareStringComparator implements Comparator<String> {

    @Contract(pure = true)
    public static Comparator<String> numbersAwareStringComparator() {
        return INSTANCE;
    }

    @Contract(pure = true)
    public static <T> Comparator<T> numbersAwareStringComparator(Function<? super T, String> extractor) {
        return (o1, o2) -> INSTANCE.compare(extractor.apply(o1), extractor.apply(o2));
    }

    private static final NumbersAwareStringComparator INSTANCE = new NumbersAwareStringComparator();

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public int compare(String string1, String string2) {
        var parts1 = splitParts(string1);
        var parts2 = splitParts(string2);
        for (int i = 0; i < min(parts1.size(), parts2.size()); i++) {
            Comparable part1 = parts1.get(i);
            Comparable part2 = parts2.get(i);
            if (part1.getClass() != part2.getClass()) {
                part1 = part1.toString();
                part2 = part2.toString();
            }
            var result = part1.compareTo(part2);
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(parts1.size(), parts2.size());
    }

    private static final Pattern ALPHA_NUM = Pattern.compile("\\d+|\\D+");

    @SuppressWarnings("rawtypes")
    private static List<Comparable> splitParts(String str) {
        var parts = new ArrayList<Comparable>();
        var matcher = ALPHA_NUM.matcher(str);
        while (matcher.find()) {
            var part = matcher.group();
            if (isDigit(part.charAt(0))) {
                parts.add(new BigInteger(part));
            } else {
                parts.add(part);
            }
        }

        return parts;
    }
}
