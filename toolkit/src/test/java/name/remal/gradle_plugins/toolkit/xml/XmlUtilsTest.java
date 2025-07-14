package name.remal.gradle_plugins.toolkit.xml;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static name.remal.gradle_plugins.toolkit.xml.XmlFormat.DEFAULT_XML_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import name.remal.gradle_plugins.toolkit.TagXslt;
import org.junit.jupiter.api.Test;

@TagXslt
@SuppressWarnings("CheckTagEmptyBody")
class XmlUtilsTest {

    @Test
    void noText() {
        var prettyXmlString = XmlUtils.prettyXmlString("<parent><node></node></parent>");
        assertThat(prettyXmlString).isEqualTo(join(
            lineSeparator(),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<parent>",
            "  <node />",
            "</parent>",
            ""
        ));
    }

    @Test
    void noSpaceBeforeTagClosing() {
        var prettyXmlString = XmlUtils.prettyXmlString(
            "<parent><node></node></parent>",
            DEFAULT_XML_FORMAT.toBuilder()
                .spaceBeforeTagClosing(false)
                .build()
        );
        assertThat(prettyXmlString).isEqualTo(join(
            lineSeparator(),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<parent>",
            "  <node/>",
            "</parent>",
            ""
        ));
    }

    @Test
    void withText() {
        var prettyXmlString = XmlUtils.prettyXmlString("<parent><node> 123 </node></parent>");
        assertThat(prettyXmlString).isEqualTo(join(
            lineSeparator(),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<parent>",
            "  <node>123</node>",
            "</parent>",
            ""
        ));
    }

    @Test
    void manyAttrs() {
        var attrs = IntStream.range(1, 150)
            .mapToObj(it -> " param" + it + "=\"value\"")
            .collect(joining());
        var prettyXmlString = XmlUtils.prettyXmlString("<parent><node" + attrs + "/></parent>");
        assertThat(prettyXmlString).isEqualTo(join(
            lineSeparator(),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<parent>",
            "  <node" + attrs + " />",
            "</parent>",
            ""
        ));
    }

}
