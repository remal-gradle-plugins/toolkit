package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.val;
import org.junit.jupiter.api.Test;

class DebugUtilsTest {

    @Test
    void dumpClassLoader() {
        val classLoader = DebugUtilsTest.class.getClassLoader();
        assertNotNull(classLoader);

        assertDoesNotThrow(() -> DebugUtils.dumpClassLoader(classLoader));
    }

}
