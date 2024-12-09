package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;

class LazyValueTest {

    @Test
    void simpleTest() {
        val lazyValue = LazyValue.lazyValue(() -> "value");
        assertFalse(lazyValue.isInitialized(), "isInitialized");
        assertEquals("value", lazyValue.get());
        assertTrue(lazyValue.isInitialized(), "isInitialized");
    }

}
