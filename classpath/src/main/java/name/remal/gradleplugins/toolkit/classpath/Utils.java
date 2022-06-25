package name.remal.gradleplugins.toolkit.classpath;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static lombok.AccessLevel.PRIVATE;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;

@NoArgsConstructor(access = PRIVATE)
abstract class Utils {

    @Unmodifiable
    public static <T> Set<T> toImmutableSet(@Nullable Iterable<T> iterable) {
        return iterable != null ? ImmutableSet.copyOf(iterable) : emptySet();
    }

    @Unmodifiable
    public static <K, E> Map<K, Set<E>> toDeepImmutableSetMap(@Nullable Map<K, Set<E>> map) {
        if (map == null) {
            return emptyMap();
        }

        val builder = ImmutableMap.<K, Set<E>>builder();
        map.forEach((key, values) -> {
            if (key != null && values != null) {
                builder.put(key, ImmutableSet.copyOf(values));
            }
        });
        return builder.build();
    }

}
