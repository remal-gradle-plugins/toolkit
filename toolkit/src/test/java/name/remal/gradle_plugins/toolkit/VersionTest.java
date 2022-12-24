package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;

class VersionTest {

    @Test
    void parse_normal() {
        val version = Version.parse("2.3.11.RELEASE");
        assertEquals(2L, version.getNumberOrNull(0));
        assertEquals(3L, version.getNumberOrNull(1));
        assertEquals(11L, version.getNumberOrNull(2));
        assertNull(version.getNumberOrNull(3));
        assertEquals("2.3.11.RELEASE", version.toString());
    }

    @Test
    void parse_suffix_only() {
        val version = Version.parse("Hoxton.SR11");
        assertNull(version.getNumberOrNull(0));
        assertEquals("Hoxton.SR11", version.toString());
    }

    @Test
    void getNumber() {
        val version = Version.parse("2.3.11.RELEASE");
        assertThrows(IllegalArgumentException.class, () -> version.getNumberOrNull(-1));
        assertThrows(IllegalArgumentException.class, () -> version.getNumber(-1));
        assertEquals(2L, version.getNumberOrNull(0));
        assertEquals(2L, version.getNumber(0));
        assertEquals(3L, version.getNumberOrNull(1));
        assertEquals(3L, version.getNumber(1));
        assertEquals(11L, version.getNumberOrNull(2));
        assertEquals(11L, version.getNumber(2));
        assertNull(version.getNumberOrNull(3));
        assertThrows(IllegalArgumentException.class, () -> version.getNumber(3));
    }

    @Test
    void withoutSuffix() {
        val version = Version.parse("2.3.11-alpha");
        assertEquals("2.3.11", version.withoutSuffix().toString());
    }

    @Test
    void isLessThan() {
        assertFalse(Version.parse("1.2").isLessThan("1.2"));
        assertTrue(Version.parse("1.2").isLessThan("1.10"));
        assertFalse(Version.parse("1.10").isLessThan("1.2"));
        assertFalse(Version.parse("1.2").isLessThan("1.2-SNAPSHOT"));
        assertTrue(Version.parse("1.2-SNAPSHOT").isLessThan("1.2"));
    }

    @Test
    void isLessOrEqualTo() {
        assertTrue(Version.parse("1.2").isLessOrEqualTo("1.2"));
        assertTrue(Version.parse("1.2").isLessOrEqualTo("1.10"));
        assertFalse(Version.parse("1.10").isLessOrEqualTo("1.2"));
        assertFalse(Version.parse("1.2").isLessOrEqualTo("1.2-SNAPSHOT"));
        assertTrue(Version.parse("1.2-SNAPSHOT").isLessOrEqualTo("1.2"));
    }

    @Test
    void isGreaterOrEqualTo() {
        assertTrue(Version.parse("1.2").isGreaterOrEqualTo("1.2"));
        assertFalse(Version.parse("1.2").isGreaterOrEqualTo("1.10"));
        assertTrue(Version.parse("1.10").isGreaterOrEqualTo("1.2"));
        assertTrue(Version.parse("1.2").isGreaterOrEqualTo("1.2-SNAPSHOT"));
        assertFalse(Version.parse("1.2-SNAPSHOT").isGreaterOrEqualTo("1.2"));
    }

    @Test
    void isGreaterThan() {
        assertFalse(Version.parse("1.2").isGreaterThan("1.2"));
        assertFalse(Version.parse("1.2").isGreaterThan("1.10"));
        assertTrue(Version.parse("1.10").isGreaterThan("1.2"));
        assertTrue(Version.parse("1.2").isGreaterThan("1.2-SNAPSHOT"));
        assertFalse(Version.parse("1.2-SNAPSHOT").isGreaterThan("1.2"));
    }

    @Test
    void equals_method() {
        val version = Version.parse("1.2");
        assertEquals(version, version);
        assertEquals(Version.parse("1.2"), Version.parse("1.2"));
    }

    @Test
    void hashCode_method() {
        assertEquals(Version.parse("1.2").hashCode(), "1.2".hashCode());
    }

}
