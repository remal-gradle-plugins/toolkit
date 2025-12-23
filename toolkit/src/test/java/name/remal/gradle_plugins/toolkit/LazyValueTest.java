package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.JavaSerializationUtils.deserializeFrom;
import static name.remal.gradle_plugins.toolkit.JavaSerializationUtils.serializeToBytes;
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

    @Test
    void serialization() {
        var original = LazyValue.lazyValue(() -> "value");

        var bytes = serializeToBytes(original);
        var deserialized = deserializeFrom(bytes, LazyValue.class);

        assertTrue(deserialized.isInitialized(), "isInitialized");
        assertEquals("value", deserialized.get());
    }

}
