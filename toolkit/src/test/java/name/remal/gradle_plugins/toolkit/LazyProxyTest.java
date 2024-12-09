package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    void asLazyListProxy() {
        {
            val lazyProxy = LazyProxy.asLazyListProxy(() ->
                singletonList("a")
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
                singleton("a")
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
    void asLazyMapProxy() {
        {
            val lazyProxy = LazyProxy.asLazyMapProxy(() ->
                singletonMap("a", "b")
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

}
