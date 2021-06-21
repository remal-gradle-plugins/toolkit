package name.remal.gradleplugins.toolkit.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WhoCalledTest {

    @Test
    void getCallingClass() {
        assertEquals(WhoCalledTest.class, WhoCalled.getCallingClass(1));
    }

    @Test
    void isCalledBy() {
        assertTrue(WhoCalled.isCalledBy(WhoCalledTest.class));
    }

}
