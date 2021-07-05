package name.remal.gradleplugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
