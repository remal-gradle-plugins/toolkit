package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InTestFlagsTest {

    @Test
    void isInTest() {
        assertTrue(InTestFlags.isInTest());
    }

    @Test
    void isInUnitTest() {
        assertTrue(InTestFlags.isInUnitTest());
    }

    @Test
    void isInFunctionalTest() {
        assertFalse(InTestFlags.isInFunctionalTest());
    }

}
