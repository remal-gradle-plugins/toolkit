package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.lang.annotation.ElementType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.val;
import org.junit.jupiter.api.Test;

class LazyProxyTest {

    @Test
    void asLazyProxy_simple() {
        val lazyProxy = LazyProxy.asLazyProxy(CharSequence.class, () -> "value");
        assertEquals("value".length(), lazyProxy.length());
    }

    @Test
    void asLazyProxy_to_string() {
        val lazyProxy = LazyProxy.asLazyProxy(CharSequence.class, () -> "value");
        assertEquals("value", lazyProxy.toString());
    }

    @Test
    @SuppressWarnings("java:S5778")
    void asLazyProxy_for_not_interface() {
        assertThrows(IllegalArgumentException.class, () ->
            LazyProxy.asLazyProxy(String.class, () -> "value")
        );

        assertThrows(IllegalArgumentException.class, () ->
            LazyProxy.asLazyProxy(ElementType.class, () -> ElementType.TYPE)
        );
    }

    @Test
    void isLazyProxy() {
        val lazyProxy = LazyProxy.asLazyProxy(CharSequence.class, () -> "value");
        assertTrue(LazyProxy.isLazyProxy(lazyProxy), "isLazyProxy: lazyProxy");
        assertFalse(LazyProxy.isLazyProxy("asd"), "isLazyProxy: string");
    }

    @Test
    void isLazyProxyInitialized() {
        val lazyProxy = LazyProxy.asLazyProxy(CharSequence.class, () -> "value");
        assertFalse(LazyProxy.isLazyProxyInitialized(lazyProxy), "isLazyProxyInitialized");

        assertEquals("value".length(), lazyProxy.length());

        assertTrue(LazyProxy.isLazyProxyInitialized(lazyProxy), "isLazyProxyInitialized");
    }

    @Test
    void isLazyProxyInitialized_failure() {
        assertThrows(IllegalArgumentException.class, () ->
            LazyProxy.isLazyProxyInitialized("asd")
        );
    }

    @Test
    void asLazyCollectionProxy() {
        {
            val lazyProxy = LazyProxy.asLazyCollectionProxy(() ->
                ImmutableList.of("a")
            );
            assertInstanceOf(Collection.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }

        {
            Collection<CharSequence> lazyProxy = LazyProxy.asLazyCollectionProxy(() ->
                ImmutableSet.of("a")
            );
            assertInstanceOf(Collection.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }
    }

    @Test
    void asLazyListProxy() {
        {
            val lazyProxy = LazyProxy.asLazyListProxy(() ->
                ImmutableList.of("a")
            );
            assertInstanceOf(List.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }

        {
            List<CharSequence> lazyProxy = LazyProxy.asLazyListProxy(() ->
                singletonList("a")
            );
            assertInstanceOf(List.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }
    }

    @Test
    void asLazySetProxy() {
        {
            val lazyProxy = LazyProxy.asLazySetProxy(() ->
                ImmutableSet.of("a")
            );
            assertInstanceOf(Set.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }

        {
            Set<CharSequence> lazyProxy = LazyProxy.asLazySetProxy(() ->
                singleton("a")
            );
            assertInstanceOf(Set.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }
    }

    @Test
    void asLazySortedSetProxy() {
        {
            val lazyProxy = LazyProxy.asLazySortedSetProxy(() ->
                ImmutableSortedSet.of("a")
            );
            assertInstanceOf(SortedSet.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }

        {
            SortedSet<CharSequence> lazyProxy = LazyProxy.asLazySortedSetProxy(() ->
                new TreeSet<>(singleton("a"))
            );
            assertInstanceOf(SortedSet.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }
    }

    @Test
    void asLazyMapProxy() {
        {
            val lazyProxy = LazyProxy.asLazyMapProxy(() ->
                ImmutableMap.of("a", "b")
            );
            assertInstanceOf(Map.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }

        {
            Map<CharSequence, CharSequence> lazyProxy = LazyProxy.asLazyMapProxy(() ->
                singletonMap("a", "b")
            );
            assertInstanceOf(Map.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }
    }

    @Test
    void asLazySortedMapProxy() {
        {
            val lazyProxy = LazyProxy.asLazySortedMapProxy(() ->
                ImmutableSortedMap.of("a", "b")
            );
            assertInstanceOf(SortedMap.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }

        {
            Map<CharSequence, CharSequence> lazyProxy = LazyProxy.asLazySortedMapProxy(() ->
                new TreeMap<>(singletonMap("a", "b"))
            );
            assertInstanceOf(SortedMap.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }
    }

    @Test
    void asLazyNavigableMapProxy() {
        {
            val lazyProxy = LazyProxy.asLazyNavigableMapProxy(() ->
                ImmutableSortedMap.of("a", "b")
            );
            assertInstanceOf(NavigableMap.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }

        {
            Map<CharSequence, CharSequence> lazyProxy = LazyProxy.asLazyNavigableMapProxy(() ->
                new TreeMap<>(singletonMap("a", "b"))
            );
            assertInstanceOf(NavigableMap.class, lazyProxy);
            assertEquals(1, lazyProxy.size());
        }
    }

}
