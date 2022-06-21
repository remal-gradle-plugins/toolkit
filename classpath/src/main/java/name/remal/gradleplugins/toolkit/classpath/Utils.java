package name.remal.gradleplugins.toolkit.classpath;

import static lombok.AccessLevel.PRIVATE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
abstract class Utils {

    public static <K, E> Map<K, Collection<E>> toDeepImmutableCollectionMap(Map<K, Collection<E>> map) {
        val builder = ImmutableMap.<K, Collection<E>>builder();
        map.forEach((key, values) -> {
            if (key != null && values != null) {
                builder.put(key, ImmutableList.copyOf(values));
            }
        });
        return builder.build();
    }

    public static <K, E> Map<K, Set<E>> toDeepMutableSetMap(Map<K, Set<E>> map) {
        Map<K, Set<E>> result = new LinkedHashMap<>();
        map.forEach((key, values) -> {
            if (values != null
                && values.getClass() != LinkedHashSet.class
                && values.getClass() != HashSet.class
                && values.getClass() != TreeSet.class
            ) {
                values = new LinkedHashSet<>(values);
            }
            result.put(key, values);
        });
        return result;
    }

    public static <K, E> Map<K, Set<E>> toDeepImmutableSetMap(Map<K, Set<E>> map) {
        val builder = ImmutableMap.<K, Set<E>>builder();
        map.forEach((key, values) -> {
            if (key != null && values != null) {
                builder.put(key, ImmutableSet.copyOf(values));
            }
        });
        return builder.build();
    }

}
