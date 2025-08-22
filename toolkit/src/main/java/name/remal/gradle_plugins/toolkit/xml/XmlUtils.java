package name.remal.gradle_plugins.toolkit.xml;

import static java.nio.file.Files.newInputStream;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.internal.JdomUtils.newNonValidatingSaxBuilder;
import static name.remal.gradle_plugins.toolkit.xml.GroovyXmlUtils.compactGroovyXmlString;
import static name.remal.gradle_plugins.toolkit.xml.GroovyXmlUtils.prettyGroovyXmlString;
import static name.remal.gradle_plugins.toolkit.xml.XmlFormat.DEFAULT_XML_FORMAT;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.jdom2.output.Format.getCompactFormat;
import static org.jdom2.output.Format.getPrettyFormat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.function.BiFunction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.jspecify.annotations.Nullable;
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

    public static Document newDocument() {
        return newNonValidatingDocumentBuilder().newDocument();
    }


    //#region parseXml()

    @SneakyThrows
    public static Document parseXml(Path path) {
        path = normalizePath(path);
        try (var inputStream = newInputStream(path)) {
            return newNonValidatingDocumentBuilder().parse(inputStream, path.toString());
        }
    }

    /**
     * See {@link #parseXml(Path)}.
     */
    @SneakyThrows
    public static Document parseXml(File file) {
        return parseXml(file.toPath());
    }

    @SneakyThrows
    public static Document parseXml(@Language("XML") String content, @Nullable String systemId) {
        var inputSource = new InputSource(new StringReader(content));
        inputSource.setSystemId(systemId);
        return newNonValidatingDocumentBuilder().parse(inputSource);
    }

    /**
     * See {@link #parseXml(String, String)}.
     */
    public static Document parseXml(@Language("XML") String content) {
        return parseXml(content, null);
    }

    @SneakyThrows
    public static Document parseXml(byte[] content, @Nullable String systemId) {
        var inputSource = new InputSource(new ByteArrayInputStream(content));
        inputSource.setSystemId(systemId);
        return newNonValidatingDocumentBuilder().parse(inputSource);
    }

    /**
     * See {@link #parseXml(String, String)}.
     */
    public static Document parseXml(byte[] content) {
        return parseXml(content, null);
    }

    //#endregion


    //#region prettyXmlString()

    @Language("XML")
    public static String prettyXmlString(@Language("XML") String xmlString) {
        return prettyXmlString(xmlString, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    @SneakyThrows
    public static String prettyXmlString(@Language("XML") String xmlString, XmlFormat format) {
        var document = newNonValidatingSaxBuilder().build(new StringReader(xmlString));
        return prettyXmlString(document, XMLOutputter::outputString, format);
    }


    @Language("XML")
    public static String prettyXmlString(groovy.util.Node groovyNode) {
        return prettyXmlString(groovyNode, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(groovy.util.Node groovyNode, XmlFormat format) {
        return prettyGroovyXmlString(groovyNode, format);
    }


    @Language("XML")
    public static String prettyXmlString(Node node) {
        return prettyXmlString(node, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
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

    @Language("XML")
    public static String prettyXmlString(Document document) {
        return prettyXmlString(document, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(Document document, XmlFormat format) {
        return prettyXmlString(document, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    @Language("XML")
    public static String prettyXmlString(Element element) {
        return prettyXmlString(element, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(Element element, XmlFormat format) {
        return prettyXmlString(element, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    @Language("XML")
    public static String prettyXmlString(CDATASection cdata) {
        return prettyXmlString(cdata, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(CDATASection cdata, XmlFormat format) {
        return prettyXmlString(cdata, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    @Language("XML")
    public static String prettyXmlString(Text text) {
        return prettyXmlString(text, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(Text text, XmlFormat format) {
        return prettyXmlString(text, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    @Language("XML")
    public static String prettyXmlString(Comment comment) {
        return prettyXmlString(comment, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(Comment comment, XmlFormat format) {
        return prettyXmlString(comment, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    @Language("XML")
    public static String prettyXmlString(ProcessingInstruction pi) {
        return prettyXmlString(pi, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(ProcessingInstruction pi, XmlFormat format) {
        return prettyXmlString(pi, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    @Language("XML")
    public static String prettyXmlString(EntityReference er) {
        return prettyXmlString(er, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(EntityReference er, XmlFormat format) {
        return prettyXmlString(er, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    @Language("XML")
    public static String prettyXmlString(DocumentType doctype) {
        return prettyXmlString(doctype, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(DocumentType doctype, XmlFormat format) {
        return prettyXmlString(doctype, DOMBuilder::build, XMLOutputter::outputString, format);
    }

    @Language("XML")
    public static String prettyXmlString(DocumentFragment documentFragment) {
        return prettyXmlString(documentFragment, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyXmlString(DocumentFragment documentFragment, XmlFormat format) {
        var insertFinalNewline = format.isInsertFinalNewline();
        format = format.toBuilder()
            .insertFinalNewline(false)
            .build();

        var result = new StringBuilder();
        var children = documentFragment.getChildNodes();
        for (int index = 0; index < children.getLength(); ++index) {
            var child = children.item(index);
            var childString = prettyXmlString(child, format);
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
        var jdomNode = toJdom.apply(DOM_BUILDER, node);
        return prettyXmlString(jdomNode, outputString, format);
    }

    private static <T> String prettyXmlString(
        T node,
        BiFunction<XMLOutputter, T, String> outputString,
        XmlFormat format
    ) {
        var outputter = new XMLOutputter(
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

    @Language("XML")
    @SneakyThrows
    public static String compactXmlString(@Language("XML") String xmlString) {
        var document = newNonValidatingSaxBuilder().build(new StringReader(xmlString));
        return compactXmlString(document, XMLOutputter::outputString);
    }


    @Language("XML")
    public static String compactXmlString(groovy.util.Node groovyNode) {
        return compactGroovyXmlString(groovyNode);
    }


    @Language("XML")
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

    @Language("XML")
    public static String compactXmlString(Document document) {
        return compactXmlString(document, DOMBuilder::build, XMLOutputter::outputString);
    }

    @Language("XML")
    public static String compactXmlString(Element element) {
        return compactXmlString(element, DOMBuilder::build, XMLOutputter::outputString);
    }

    @Language("XML")
    public static String compactXmlString(CDATASection cdata) {
        return compactXmlString(cdata, DOMBuilder::build, XMLOutputter::outputString);
    }

    @Language("XML")
    public static String compactXmlString(Text text) {
        return compactXmlString(text, DOMBuilder::build, XMLOutputter::outputString);
    }

    @Language("XML")
    public static String compactXmlString(Comment comment) {
        return compactXmlString(comment, DOMBuilder::build, XMLOutputter::outputString);
    }

    @Language("XML")
    public static String compactXmlString(ProcessingInstruction pi) {
        return compactXmlString(pi, DOMBuilder::build, XMLOutputter::outputString);
    }

    @Language("XML")
    public static String compactXmlString(EntityReference er) {
        return compactXmlString(er, DOMBuilder::build, XMLOutputter::outputString);
    }

    @Language("XML")
    public static String compactXmlString(DocumentType doctype) {
        return compactXmlString(doctype, DOMBuilder::build, XMLOutputter::outputString);
    }

    @Language("XML")
    public static String compactXmlString(DocumentFragment documentFragment) {
        var result = new StringBuilder();
        var children = documentFragment.getChildNodes();
        for (int index = 0; index < children.getLength(); ++index) {
            var child = children.item(index);
            result.append(compactXmlString(child));
        }
        return result.toString();
    }

    private static <DOM extends Node, JDOM> String compactXmlString(
        DOM node,
        BiFunction<DOMBuilder, DOM, JDOM> toJdom,
        BiFunction<XMLOutputter, JDOM, String> outputString
    ) {
        var jdomNode = toJdom.apply(DOM_BUILDER, node);
        return compactXmlString(jdomNode, outputString);
    }

    private static <T> String compactXmlString(
        T node,
        BiFunction<XMLOutputter, T, String> outputString
    ) {
        var isDocument = node instanceof org.jdom2.Document;
        var outputter = new XMLOutputter(
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

    public static void tryToSetXmlSetting(DocumentBuilderFactory factory, String name, @Nullable Object value) {
        tryToSetXmlSetting(factory::setAttribute, factory::setFeature, name, value);
    }

    public static void tryToSetXmlSetting(TransformerFactory factory, String name, @Nullable Object value) {
        tryToSetXmlSetting(factory::setAttribute, factory::setFeature, name, value);
    }

    public static void tryToSetXmlSetting(XPathFactory factory, String name, @Nullable Object value) {
        tryToSetXmlSetting(null, factory::setFeature, name, value);
    }

    @SuppressWarnings("java:S3776")
    private static void tryToSetXmlSetting(
        @Nullable XmlAttributeSetter attributeSetter,
        @Nullable XmlFeatureSetter featureSetter,
        String name,
        @Nullable Object value
    ) {
        if (attributeSetter != null) {
            try {
                attributeSetter.set(name, value);
            } catch (IllegalArgumentException ignored) {
                // do nothing
            }
        }


        if (featureSetter != null) {
            Boolean booleanValue = null;
            if (value instanceof Boolean) {
                booleanValue = (Boolean) value;
            } else if (value instanceof CharSequence) {
                if ("true".equalsIgnoreCase(value.toString())) {
                    booleanValue = true;
                } else if ("false".equalsIgnoreCase(value.toString())) {
                    booleanValue = false;
                }
            }

            if (booleanValue != null) {
                try {
                    featureSetter.set(name, booleanValue);
                } catch (TransformerConfigurationException
                         | ParserConfigurationException
                         | XPathFactoryConfigurationException
                    ignored
                ) {
                    // do nothing
                }
            }
        }
    }

    @FunctionalInterface
    private interface XmlAttributeSetter {
        void set(String name, @Nullable Object value);
    }

    @FunctionalInterface
    private interface XmlFeatureSetter {
        void set(String name, boolean value) throws
            TransformerConfigurationException,
            ParserConfigurationException,
            XPathFactoryConfigurationException;
    }

    //#endregion


    //#region utilities

    private static final DOMBuilder DOM_BUILDER = new DOMBuilder();

    private static final DocumentBuilderFactory NON_VALIDATING_DOCUMENT_BUILDER_FACTORY;

    static {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        tryToSetXmlSetting(factory, FEATURE_SECURE_PROCESSING, true);
        NON_VALIDATING_DOCUMENT_BUILDER_FACTORY = factory;
    }

    @SneakyThrows
    private static DocumentBuilder newNonValidatingDocumentBuilder() {
        var documentBuilder = NON_VALIDATING_DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        documentBuilder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        return documentBuilder;
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
            org.jdom2.@Nullable Comment comment
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
