package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.XmlFormat.DEFAULT_XML_FORMAT;
import static org.jdom2.input.sax.XMLReaders.NONVALIDATING;
import static org.jdom2.output.Format.getPrettyFormat;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jdom2.Comment;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.xml.sax.InputSource;

@NoArgsConstructor(access = PRIVATE)
public abstract class XmlUtils {

    private static final SAXBuilder NON_VALIDATING_SAX_BUILDER;

    static {
        val saxBuilder = new SAXBuilder();
        saxBuilder.setXMLReaderFactory(NONVALIDATING);
        saxBuilder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        saxBuilder.setReuseParser(true);
        NON_VALIDATING_SAX_BUILDER = saxBuilder;
    }


    public static String prettyXmlString(String xmlString) {
        return prettyXmlString(xmlString, DEFAULT_XML_FORMAT);
    }

    @SneakyThrows
    public static String prettyXmlString(String xmlString, XmlFormat format) {
        val saxBuilder = NON_VALIDATING_SAX_BUILDER;
        val document = saxBuilder.build(new StringReader(xmlString));

        val outputter = new XMLOutputter(
            getPrettyFormat()
                .setEncoding(format.getCharset().name())
                .setOmitDeclaration(format.isOmitDeclaration())
                .setOmitEncoding(format.isOmitEncoding())
                .setIndent(format.getIndent())
                .setLineSeparator(format.getLineSeparator()),
            format.isSpaceBeforeTagClosing() ? null : new ExtendedXmlOutputProcessor()
        );

        xmlString = outputter.outputString(document);

        if (!format.isInsertFinalNewline()) {
            xmlString = xmlString.trim();
        }

        return xmlString;
    }


    private static class ExtendedXmlOutputProcessor
        extends AbstractXMLOutputProcessor
        implements XMLOutputProcessor {

        private boolean removeExtraSpace = true;

        @Override
        protected void write(Writer out, String str) throws IOException {
            if (removeExtraSpace) {
                if (str.equals(" />")) {
                    str = "/>";
                }
            }
            super.write(out, str);
        }

        @Override
        protected void textRaw(Writer out, String text) throws IOException {
            removeExtraSpace = false;
            try {
                super.textRaw(out, text);
            } finally {
                removeExtraSpace = true;
            }
        }

        @Override
        protected void printComment(Writer out, FormatStack fstack, Comment comment) throws IOException {
            removeExtraSpace = false;
            try {
                super.printComment(out, fstack, comment);
            } finally {
                removeExtraSpace = true;
            }
        }
    }

}
