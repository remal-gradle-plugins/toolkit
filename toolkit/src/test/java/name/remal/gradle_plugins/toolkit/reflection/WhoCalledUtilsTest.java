package name.remal.gradle_plugins.toolkit.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WhoCalledUtilsTest {

    @Test
    void getCallingClass() {
        assertEquals(WhoCalledUtilsTest.class, WhoCalledUtils.getCallingClass(1));
    }

    @Test
    void isCalledBy() {
        assertTrue(WhoCalledUtils.isCalledBy(WhoCalledUtilsTest.class));
    }

}
