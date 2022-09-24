package name.remal.gradleplugins.toolkit.xml;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static name.remal.gradleplugins.toolkit.xml.XmlUtils.compactXmlString;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;
import org.junit.jupiter.api.Test;

class XmlProviderImplTest {

    @Test
    void asString() {
        val xmlProvider = new XmlProviderImpl("<parent b=\"b\" a=\"a\"><child>123</child></parent>");
        val string = xmlProvider.asString();
        assertThat(string).asString().isEqualTo(join(
            lineSeparator(),
            "<parent b=\"b\" a=\"a\">",
            "  <child>123</child>",
            "</parent>",
            ""
        ));
    }

    @Test
    void asNode() {
        val xmlProvider = new XmlProviderImpl("<parent b=\"b\" a=\"a\"><child>123</child></parent>");
        val node = xmlProvider.asNode();
        val nodeString = compactXmlString(node);
        assertThat(nodeString).asString().isEqualTo("<parent b=\"b\" a=\"a\"><child>123</child></parent>");
    }

    @Test
    void asElement() {
        val xmlProvider = new XmlProviderImpl("<parent b=\"b\" a=\"a\"><child>123</child></parent>");
        val element = xmlProvider.asElement();
        val elementString = compactXmlString(element);
        assertThat(elementString).asString().isEqualTo("<parent a=\"a\" b=\"b\"><child>123</child></parent>");
    }

}
