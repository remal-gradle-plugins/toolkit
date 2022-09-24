package name.remal.gradleplugins.toolkit;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMap;
import lombok.val;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    void normalizeString() {
        assertEquals("", StringUtils.normalizeString(" \r\n\r "));

        assertEquals("b\ne", StringUtils.normalizeString("b\r\ne"));
        assertEquals("b\ne", StringUtils.normalizeString("b\n\re"));
        assertEquals("b\ne", StringUtils.normalizeString("b\re"));

        assertEquals("a", StringUtils.normalizeString("a \t "));
        assertEquals("a", StringUtils.normalizeString("a \t \n"));
        assertEquals("b\n e", StringUtils.normalizeString("b \t \n e"));

        assertEquals("b\n\ne", StringUtils.normalizeString("b\n\n\ne"));

        assertEquals(" a b", StringUtils.normalizeString("\n\n a b "));
        assertEquals(" a b", StringUtils.normalizeString("\t\n \n a b "));
        assertEquals(" a b", StringUtils.normalizeString(" a b \n\n"));
        assertEquals(" a b", StringUtils.normalizeString(" a b \n\t\n "));
    }

    @Test
    void indentString() {
        assertEquals("", StringUtils.indentString(" \r\n\r "));

        assertEquals("  a", StringUtils.indentString("a"));
        assertEquals("  a\n  b", StringUtils.indentString("a\nb"));

        assertEquals("  a", StringUtils.indentString("a \t\n"));

        assertEquals("  a\n\n  b", StringUtils.indentString("a\n\t\n \nb"));
    }


    @Test
    void trimWith() {
        val mapping = ImmutableMap.<String, String>builder()
            .put("", "")
            .put("1", "")
            .put("12", "")
            .put("21", "")
            .put("1a", "a")
            .put("122a", "a")
            .put("211a", "a")
            .put("a1", "a")
            .put("a122", "a")
            .put("a211", "a")
            .put("1a2", "a")
            .put("12a122", "a")
            .put("21a211", "a")
            .build();
        for (val entry : mapping.entrySet()) {
            assertEquals(
                entry.getKey(),
                StringUtils.trimWith(entry.getKey(), ""),
                format("trimWith('%s', '%s')", entry.getKey(), "")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimWith(entry.getKey(), "112"),
                format("trimWith('%s', '%s')", entry.getKey(), "112")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimWith(entry.getKey(), "211"),
                format("trimWith('%s', '%s')", entry.getKey(), "211")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimWith(entry.getKey(), '1', '1', '2'),
                format("trimWith('%s', '%s')", entry.getKey(), "112")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimWith(entry.getKey(), '1', '2', '1', '2'),
                format("trimWith('%s', '%s')", entry.getKey(), "211")
            );
        }
    }

    @Test
    void trimLeftWith() {
        val mapping = ImmutableMap.<String, String>builder()
            .put("", "")
            .put("1", "")
            .put("12", "")
            .put("21", "")
            .put("1a", "a")
            .put("122a", "a")
            .put("211a", "a")
            .put("a1", "a1")
            .put("a122", "a122")
            .put("a211", "a211")
            .put("1a2", "a2")
            .put("12a122", "a122")
            .put("21a211", "a211")
            .build();
        for (val entry : mapping.entrySet()) {
            assertEquals(
                entry.getKey(),
                StringUtils.trimLeftWith(entry.getKey(), ""),
                format("trimLeftWith('%s', '%s')", entry.getKey(), "")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimLeftWith(entry.getKey(), "112"),
                format("trimLeftWith('%s', '%s')", entry.getKey(), "112")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimLeftWith(entry.getKey(), "211"),
                format("trimLeftWith('%s', '%s')", entry.getKey(), "211")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimLeftWith(entry.getKey(), '1', '1', '2'),
                format("trimLeftWith('%s', '%s')", entry.getKey(), "112")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimLeftWith(entry.getKey(), '1', '2', '1', '2'),
                format("trimLeftWith('%s', '%s')", entry.getKey(), "211")
            );
        }
    }

    @Test
    void trimRightWith() {
        val mapping = ImmutableMap.<String, String>builder()
            .put("", "")
            .put("1", "")
            .put("12", "")
            .put("21", "")
            .put("1a", "1a")
            .put("122a", "122a")
            .put("211a", "211a")
            .put("a1", "a")
            .put("a122", "a")
            .put("a211", "a")
            .put("1a2", "1a")
            .put("12a122", "12a")
            .put("21a211", "21a")
            .build();
        for (val entry : mapping.entrySet()) {
            assertEquals(
                entry.getKey(),
                StringUtils.trimRightWith(entry.getKey(), ""),
                format("trimRightWith('%s', '%s')", entry.getKey(), "")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimRightWith(entry.getKey(), "112"),
                format("trimRightWith('%s', '%s')", entry.getKey(), "112")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimRightWith(entry.getKey(), "211"),
                format("trimRightWith('%s', '%s')", entry.getKey(), "211")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimRightWith(entry.getKey(), '1', '1', '2'),
                format("trimRightWith('%s', '%s')", entry.getKey(), "112")
            );
            assertEquals(
                entry.getValue(),
                StringUtils.trimRightWith(entry.getKey(), '1', '2', '1', '2'),
                format("trimRightWith('%s', '%s')", entry.getKey(), "211")
            );
        }
    }

}
