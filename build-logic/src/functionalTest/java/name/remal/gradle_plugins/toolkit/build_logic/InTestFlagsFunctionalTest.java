package name.remal.gradle_plugins.toolkit.build_logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import name.remal.gradle_plugins.toolkit.InTestFlags;
import org.junit.jupiter.api.Test;

class InTestFlagsFunctionalTest {

    @Test
    void isInTest() {
        assertTrue(InTestFlags.isInTest());
    }

    @Test
    void isInUnitTest() {
        assertFalse(InTestFlags.isInUnitTest());
    }

    @Test
    void isInFunctionalTest() {
        assertTrue(InTestFlags.isInFunctionalTest());
    }

}
