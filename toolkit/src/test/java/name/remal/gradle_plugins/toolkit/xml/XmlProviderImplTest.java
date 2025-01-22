package name.remal.gradle_plugins.toolkit.xml;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static name.remal.gradle_plugins.toolkit.xml.XmlUtils.compactXmlString;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class XmlProviderImplTest {

    @Test
    void asString() {
        var xmlProvider = new XmlProviderImpl("<parent b=\"b\" a=\"a\"><child>123</child></parent>");
        var string = xmlProvider.asString();
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
        var xmlProvider = new XmlProviderImpl("<parent b=\"b\" a=\"a\"><child>123</child></parent>");
        var node = xmlProvider.asNode();
        var nodeString = compactXmlString(node);
        assertThat(nodeString).asString().isEqualTo("<parent b=\"b\" a=\"a\"><child>123</child></parent>");
    }

    @Test
    void asElement() {
        var xmlProvider = new XmlProviderImpl("<parent b=\"b\" a=\"a\"><child>123</child></parent>");
        var element = xmlProvider.asElement();
        var elementString = compactXmlString(element);
        assertThat(elementString).asString().isEqualTo("<parent a=\"a\" b=\"b\"><child>123</child></parent>");
    }

}
