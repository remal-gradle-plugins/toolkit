package name.remal.gradle_plugins.toolkit.cache.files;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.SortedMap;

@FunctionalInterface
public interface ToolkitFilesCacheKeyEncoder<KEY> {

    /**
     * Must return a stable string representation of the key.
     *
     * <p>If string representation is unstable
     * (for example, {@code toString()} of {@link HashMap} instead of {@link SortedMap} or {@link LinkedHashMap}),
     * the cache may produce incorrect results.
     */
    String encode(KEY key) throws Throwable;

}
