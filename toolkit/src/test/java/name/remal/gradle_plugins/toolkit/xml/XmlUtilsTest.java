package name.remal.gradle_plugins.toolkit.xml;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static name.remal.gradle_plugins.toolkit.xml.XmlFormat.DEFAULT_XML_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import lombok.val;
import name.remal.gradle_plugins.toolkit.TagXslt;
import org.junit.jupiter.api.Test;

@TagXslt
@SuppressWarnings("CheckTagEmptyBody")
class XmlUtilsTest {

    @Test
    void noText() {
        val prettyXmlString = XmlUtils.prettyXmlString("<parent><node></node></parent>");
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
        val prettyXmlString = XmlUtils.prettyXmlString(
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
        val prettyXmlString = XmlUtils.prettyXmlString("<parent><node> 123 </node></parent>");
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
        val attrs = IntStream.range(1, 1000)
            .mapToObj(it -> " param" + it + "=\"value\"")
            .collect(joining());
        val prettyXmlString = XmlUtils.prettyXmlString("<parent><node" + attrs + "/></parent>");
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
