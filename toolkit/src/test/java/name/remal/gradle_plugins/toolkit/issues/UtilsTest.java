package name.remal.gradle_plugins.toolkit.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void compareOptionals() {
        assertEquals(-1, Utils.compareOptionals(1, 2));
        assertEquals(0, Utils.compareOptionals(1, 1));
        assertEquals(1, Utils.compareOptionals(2, 1));

        assertEquals(-1, Utils.compareOptionals(1, null));
        assertEquals(1, Utils.compareOptionals(null, 1));
        assertEquals(0, Utils.compareOptionals(null, null));
    }

}
