package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;

class ProxyUtilsTest {

    @Test
    void isEqualsMethod() throws Throwable {
        val method = Object.class.getMethod("equals", Object.class);
        assertTrue(ProxyUtils.isEqualsMethod(method));
    }

    @Test
    void isHashCodeMethod() throws Throwable {
        val method = Object.class.getMethod("hashCode");
        assertTrue(ProxyUtils.isHashCodeMethod(method));
    }

    @Test
    void isToStringMethod() throws Throwable {
        val method = Object.class.getMethod("toString");
        assertTrue(ProxyUtils.isToStringMethod(method));
    }


    @Test
    void toDynamicInterface() {
        val dynamicInterface = ProxyUtils.toDynamicInterface(new DynamicInterfaceImpl(), DynamicInterface.class);

        // normal method:
        assertEquals("value", dynamicInterface.getValue());

        // default method:
        assertEquals("default", dynamicInterface.getDefaultValue());

        // wrong method:
        assertThrows(UnsupportedOperationException.class, dynamicInterface::getOtherValue);
    }

    @SuppressWarnings({"UnusedMethod", "unused"})
    private static class DynamicInterfaceImpl {
        public String getValue() {
            return "value";
        }
    }

    @SuppressWarnings({"UnusedMethod", "unused"})
    private interface DynamicInterface {
        String getValue();

        String getOtherValue();

        default String getDefaultValue() {
            return "default";
        }
    }

}
