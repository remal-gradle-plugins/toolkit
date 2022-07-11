package name.remal.gradleplugins.toolkit.xml;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.xml.XmlFormat.DEFAULT_XML_FORMAT;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.jdom2.input.sax.XMLReaders.NONVALIDATING;
import static org.jdom2.output.Format.getCompactFormat;
import static org.jdom2.output.Format.getPrettyFormat;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

@NoArgsConstructor(access = PRIVATE)
public abstract class XmlUtils {

    //#region prettyXmlString()

    public static String prettyXmlString(String xmlString) {
        return prettyXmlString(xmlString, DEFAULT_XML_FORMAT);
    }

    @SneakyThrows
    public static String prettyXmlString(String xmlString, XmlFormat format) {
        val document = NON_VALIDATING_SAX_BUILDER.build(new StringReader(xmlString));
        return prettyXmlString(document, XMLOutputter::outputString, format);
    }


    public static String prettyXmlString(Node node) {
        return prettyXmlString(node, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(Node node, XmlFormat format) {
        if (node instanceof Document) {
            return prettyXmlString((Document) node, format);
        } else if (node instanceof Element) {
            return prettyXmlString((Element) node, format);
        } else if (node instanceof CDATASection) {
            return prettyXmlString((CDATASection) node, format);
        } else if (node instanceof Text) {
            return prettyXmlString((Text) node, format);
        } else if (node instanceof Comment) {
            return prettyXmlString((Comment) node, format);
        } else if (node instanceof ProcessingInstruction) {
            return prettyXmlString((ProcessingInstruction) node, format);
        } else if (node instanceof EntityReference) {
            return prettyXmlString((EntityReference) node, format);
        } else if (node instanceof DocumentType) {
            return prettyXmlString((DocumentType) node, format);
        } else if (node instanceof DocumentFragment) {
            return prettyXmlString((DocumentFragment) node);
        } else {
            throw new UnsupportedOperationException("Unsupported node type: " + node.getClass());
        }
    }

    public static String prettyXmlString(Document document) {
        return prettyXmlString(document, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(Document document, XmlFormat format) {
        return prettyXmlString(document, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    public static String prettyXmlString(Element element) {
        return prettyXmlString(element, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(Element element, XmlFormat format) {
        return prettyXmlString(element, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    public static String prettyXmlString(CDATASection cdata) {
        return prettyXmlString(cdata, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(CDATASection cdata, XmlFormat format) {
        return prettyXmlString(cdata, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    public static String prettyXmlString(Text text) {
        return prettyXmlString(text, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(Text text, XmlFormat format) {
        return prettyXmlString(text, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    public static String prettyXmlString(Comment comment) {
        return prettyXmlString(comment, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(Comment comment, XmlFormat format) {
        return prettyXmlString(comment, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    public static String prettyXmlString(ProcessingInstruction pi) {
        return prettyXmlString(pi, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(ProcessingInstruction pi, XmlFormat format) {
        return prettyXmlString(pi, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    public static String prettyXmlString(EntityReference er) {
        return prettyXmlString(er, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(EntityReference er, XmlFormat format) {
        return prettyXmlString(er, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    public static String prettyXmlString(DocumentType doctype) {
        return prettyXmlString(doctype, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(DocumentType doctype, XmlFormat format) {
        return prettyXmlString(doctype, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    public static String prettyXmlString(DocumentFragment documentFragment) {
        return prettyXmlString(documentFragment, DEFAULT_XML_FORMAT);
    }

    public static String prettyXmlString(DocumentFragment documentFragment, XmlFormat format) {
        val insertFinalNewline = format.isInsertFinalNewline();
        format = format.toBuilder()
            .insertFinalNewline(false)
            .build();

        val result = new StringBuilder();
        val children = documentFragment.getChildNodes();
        for (int index = 0; index < children.getLength(); ++index) {
            val child = children.item(index);
            val childString = prettyXmlString(child, format);
            if (isNotEmpty(childString)) {
                if (isNotEmpty(result)) {
                    result.append(format.getLineSeparator());
                }
                result.append(childString);
            }
        }

        if (isNotEmpty(result) && insertFinalNewline) {
            result.append(format.getLineSeparator());
        }

        return result.toString();
    }

    private static <DOM extends Node, JDOM> String prettyXmlString(
        DOM node,
        BiFunction<DOMBuilder, DOM, JDOM> toJdom,
        BiFunction<XMLOutputter, JDOM, String> outputString,
        XmlFormat format
    ) {
        val jdomNode = toJdom.apply(DOM_BUILDER, node);
        return prettyXmlString(jdomNode, outputString, format);
    }

    private static <T> String prettyXmlString(
        T node,
        BiFunction<XMLOutputter, T, String> outputString,
        XmlFormat format
    ) {
        val outputter = new XMLOutputter(
            getPrettyFormat()
                .setEncoding(format.getCharset().name())
                .setOmitDeclaration(format.isOmitDeclaration())
                .setOmitEncoding(format.isOmitEncoding())
                .setIndent(format.getIndent())
                .setLineSeparator(format.getLineSeparator()),
            format.isSpaceBeforeTagClosing() ? null : new ExtendedXmlOutputProcessor()
        );

        String xmlString = outputString.apply(outputter, node);

        if (!format.isInsertFinalNewline()) {
            xmlString = xmlString.trim();
        }

        return xmlString;
    }

    //#endregion


    //#region compactXmlString()

    @SneakyThrows
    public static String compactXmlString(String xmlString) {
        val document = NON_VALIDATING_SAX_BUILDER.build(new StringReader(xmlString));
        return compactXmlString(document, XMLOutputter::outputString);
    }

    public static String compactXmlString(Node node) {
        if (node instanceof Document) {
            return compactXmlString((Document) node);
        } else if (node instanceof Element) {
            return compactXmlString((Element) node);
        } else if (node instanceof CDATASection) {
            return compactXmlString((CDATASection) node);
        } else if (node instanceof Text) {
            return compactXmlString((Text) node);
        } else if (node instanceof Comment) {
            return compactXmlString((Comment) node);
        } else if (node instanceof ProcessingInstruction) {
            return compactXmlString((ProcessingInstruction) node);
        } else if (node instanceof EntityReference) {
            return compactXmlString((EntityReference) node);
        } else if (node instanceof DocumentType) {
            return compactXmlString((DocumentType) node);
        } else if (node instanceof DocumentFragment) {
            return compactXmlString((DocumentFragment) node);
        } else {
            throw new UnsupportedOperationException("Unsupported node type: " + node.getClass());
        }
    }

    public static String compactXmlString(Document document) {
        return compactXmlString(document, DOMBuilder::build, XMLOutputter::outputString);
    }

    public static String compactXmlString(Element element) {
        return compactXmlString(element, DOMBuilder::build, XMLOutputter::outputString);
    }

    public static String compactXmlString(CDATASection cdata) {
        return compactXmlString(cdata, DOMBuilder::build, XMLOutputter::outputString);
    }

    public static String compactXmlString(Text text) {
        return compactXmlString(text, DOMBuilder::build, XMLOutputter::outputString);
    }

    public static String compactXmlString(Comment comment) {
        return compactXmlString(comment, DOMBuilder::build, XMLOutputter::outputString);
    }

    public static String compactXmlString(ProcessingInstruction pi) {
        return compactXmlString(pi, DOMBuilder::build, XMLOutputter::outputString);
    }

    public static String compactXmlString(EntityReference er) {
        return compactXmlString(er, DOMBuilder::build, XMLOutputter::outputString);
    }

    public static String compactXmlString(DocumentType doctype) {
        return compactXmlString(doctype, DOMBuilder::build, XMLOutputter::outputString);
    }

    public static String compactXmlString(DocumentFragment documentFragment) {
        val result = new StringBuilder();
        val children = documentFragment.getChildNodes();
        for (int index = 0; index < children.getLength(); ++index) {
            val child = children.item(index);
            result.append(compactXmlString(child));
        }
        return result.toString();
    }

    private static <DOM extends Node, JDOM> String compactXmlString(
        DOM node,
        BiFunction<DOMBuilder, DOM, JDOM> toJdom,
        BiFunction<XMLOutputter, JDOM, String> outputString
    ) {
        val jdomNode = toJdom.apply(DOM_BUILDER, node);
        return compactXmlString(jdomNode, outputString);
    }

    private static <T> String compactXmlString(
        T node,
        BiFunction<XMLOutputter, T, String> outputString
    ) {
        val isDocument = node instanceof org.jdom2.Document;
        val outputter = new XMLOutputter(
            getCompactFormat()
                .setOmitDeclaration(!isDocument)
                .setOmitEncoding(!isDocument),
            new ExtendedXmlOutputProcessor()
        );

        String xmlString = outputString.apply(outputter, node);
        xmlString = xmlString.trim();

        return xmlString;
    }

    //#endregion


    //#region utilities

    private static final DOMOutputter DOM_OUTPUTTER = new DOMOutputter();

    private static final DOMBuilder DOM_BUILDER = new DOMBuilder();

    private static final SAXBuilder NON_VALIDATING_SAX_BUILDER;

    static {
        val saxBuilder = new SAXBuilder();
        saxBuilder.setXMLReaderFactory(NONVALIDATING);
        saxBuilder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        saxBuilder.setReuseParser(true);
        NON_VALIDATING_SAX_BUILDER = saxBuilder;
    }

    private static class ExtendedXmlOutputProcessor
        extends AbstractXMLOutputProcessor
        implements XMLOutputProcessor {

        private boolean removeExtraSpace = true;

        @Override
        protected void write(Writer out, @Nullable String str) throws IOException {
            if (removeExtraSpace) {
                if (str != null && str.equals(" />")) {
                    str = "/>";
                }
            }
            super.write(out, str);
        }

        @Override
        protected void textRaw(Writer out, @Nullable String text) throws IOException {
            removeExtraSpace = false;
            try {
                super.textRaw(out, text);
            } finally {
                removeExtraSpace = true;
            }
        }

        @Override
        protected void printComment(
            Writer out,
            @Nullable FormatStack fstack,
            @Nullable org.jdom2.Comment comment
        ) throws IOException {
            removeExtraSpace = false;
            try {
                super.printComment(out, fstack, comment);
            } finally {
                removeExtraSpace = true;
            }
        }
    }

    //#endregion

}
