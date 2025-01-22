package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LazyValueTest {

    @Test
    void simpleTest() {
        var lazyValue = LazyValue.lazyValue(() -> "value");
        assertFalse(lazyValue.isInitialized(), "isInitialized");
        assertEquals("value", lazyValue.get());
        assertTrue(lazyValue.isInitialized(), "isInitialized");
    }

}
