package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import lombok.val;
import org.junit.jupiter.api.Test;

class LazyProxyTest {

    @Test
    void asLazyProxy() {
        val lazyValue = LazyValue.of(() -> "value");
        val lazyProxy = LazyProxy.asLazyProxy(CharSequence.class, lazyValue);
        assertFalse(lazyValue.isInitialized(), "isInitialized");

        assertEquals("value".length(), lazyProxy.length());

        assertTrue(lazyValue.isInitialized(), "isInitialized");
    }

    @Test
    void asLazyProxy_to_string() {
        val lazyValue = LazyValue.of(() -> "value");
        val lazyProxy = LazyProxy.asLazyProxy(CharSequence.class, lazyValue);
        assertEquals("value", lazyProxy.toString());
    }

    @Test
    @SuppressWarnings("java:S5778")
    void asLazyProxy_for_not_interface() {
        assertThrows(IllegalArgumentException.class, () ->
            LazyProxy.asLazyProxy(String.class, LazyValue.of(() -> "value"))
        );

        assertThrows(IllegalArgumentException.class, () ->
            LazyProxy.asLazyProxy(ElementType.class, LazyValue.of(() -> ElementType.TYPE))
        );
    }

    @Test
    void isLazyProxy() {
        val lazyProxy = LazyProxy.asLazyProxy(CharSequence.class, LazyValue.of(() -> "value"));
        assertTrue(LazyProxy.isLazyProxy(lazyProxy), "isLazyProxy");
    }

}
