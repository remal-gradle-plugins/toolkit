package name.remal.gradleplugins.toolkit.xml;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Objects.requireNonNull;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradleplugins.toolkit.xml.DomUtils.getNodeOwnerDocument;
import static name.remal.gradleplugins.toolkit.xml.GroovyXmlUtils.parseXmlToGroovyNode;
import static name.remal.gradleplugins.toolkit.xml.XmlFormat.DEFAULT_XML_FORMAT;
import static name.remal.gradleplugins.toolkit.xml.XmlUtils.parseXml;
import static name.remal.gradleplugins.toolkit.xml.XmlUtils.prettyXmlString;

import groovy.util.Node;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.XmlProvider;
import org.intellij.lang.annotations.Language;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@NotThreadSafe
public class XmlProviderImpl implements XmlProvider {

    @SneakyThrows
    public static XmlProvider newXmlProviderForFile(Path path) {
        path = normalizePath(path);
        val contentBytes = readAllBytes(path);
        val charset = Optional.ofNullable(parseXml(contentBytes).getXmlEncoding())
            .map(Charset::forName)
            .orElse(UTF_8);
        val contentString = new String(contentBytes, charset);
        return new XmlProviderImpl(contentString);
    }

    public static XmlProvider newXmlProviderForFile(File file) {
        return newXmlProviderForFile(file.toPath());
    }


    private static final XmlFormat NO_DECLARATION_XML_FORMAT = DEFAULT_XML_FORMAT
        .withOmitDeclaration(true)
        .withOmitEncoding(true);


    @Nullable
    private StringBuilder string;

    @Nullable
    private Node node;

    @Nullable
    private Element element;

    public XmlProviderImpl(@Language("XML") String string) {
        this.string = new StringBuilder(prettyXmlString(string, NO_DECLARATION_XML_FORMAT));
    }

    public XmlProviderImpl(Node node) {
        this.node = node;
    }

    public XmlProviderImpl(Element element) {
        this.element = element;
    }

    public XmlProviderImpl(Document document) {
        this.element = requireNonNull(document.getDocumentElement(), "document element");
    }


    public void writeTo(Writer writer) {
        writeTo(writer, DEFAULT_XML_FORMAT);
    }

    @SneakyThrows
    public void writeTo(Writer writer, XmlFormat xmlFormat) {
        if (string != null) {
            writer.write(prettyXmlString(string.toString(), xmlFormat));
        } else if (node != null) {
            writer.write(prettyXmlString(node, xmlFormat));
        } else if (element != null) {
            val document = getNodeOwnerDocument(element);
            writer.write(prettyXmlString(document, xmlFormat));
        } else {
            throw new IllegalStateException();
        }
    }

    public void writeTo(OutputStream outputStream) {
        writeTo(outputStream, DEFAULT_XML_FORMAT);
    }

    @SneakyThrows
    public void writeTo(OutputStream outputStream, XmlFormat xmlFormat) {
        try (val writer = new OutputStreamWriter(outputStream, xmlFormat.getCharset())) {
            writeTo(writer, xmlFormat);
        }
    }


    @Override
    public StringBuilder asString() {
        if (string == null) {
            if (node != null) {
                string = new StringBuilder(prettyXmlString(node, NO_DECLARATION_XML_FORMAT));
                node = null;
            } else if (element != null) {
                string = new StringBuilder(prettyXmlString(element, NO_DECLARATION_XML_FORMAT));
                element = null;
            } else {
                throw new IllegalStateException();
            }
        }
        return string;
    }

    @Override
    @SneakyThrows
    public Node asNode() {
        if (node == null) {
            node = parseXmlToGroovyNode(asString().toString());
            string = null;
        }
        return node;
    }

    @Override
    public Element asElement() {
        if (element == null) {
            val document = parseXml(asString().toString());
            element = requireNonNull(document.getDocumentElement(), "document element");
            string = null;
        }
        return element;
    }

}
