package name.remal.gradle_plugins.toolkit.xml;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GroovyXmlUtilsTest {

    @Test
    void test() {
        var xml = "<parent b=\"b\" a=\"a\"><child>123</child></parent>";
        var node = GroovyXmlUtils.parseXmlToGroovyNode(xml);

        assertThat(GroovyXmlUtils.prettyGroovyXmlString(node)).isEqualTo(join(
            lineSeparator(),
            "<parent b=\"b\" a=\"a\">",
            "  <child>",
            "    123",
            "  </child>",
            "</parent>",
            ""
        ));

        assertThat(GroovyXmlUtils.compactGroovyXmlString(node)).isEqualTo(xml);
    }

}
