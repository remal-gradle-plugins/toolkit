package name.remal.gradleplugins.toolkit.xml;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.With;

@Value
@Builder(toBuilder = true)
@With
public class XmlFormat {

    public static final XmlFormat DEFAULT_XML_FORMAT = XmlFormat.builder().build();


    @Default
    boolean omitDeclaration = false;

    @Default
    boolean omitEncoding = false;

    @Default
    String lineSeparator = lineSeparator();

    @Default
    Charset charset = UTF_8;

    @Default
    String indent = "  ";

    @Default
    boolean spaceBeforeTagClosing = true;

    @Default
    boolean insertFinalNewline = true;

}
