package name.remal.gradleplugins.toolkit.xml;

import static java.nio.file.Files.newInputStream;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradleplugins.toolkit.SneakyThrowUtils.sneakyThrow;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.getOptionalMethod;
import static name.remal.gradleplugins.toolkit.reflection.MethodsInvoker.invokeMethod;
import static name.remal.gradleplugins.toolkit.xml.XmlFormat.DEFAULT_XML_FORMAT;

import groovy.util.IndentPrinter;
import groovy.util.Node;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.intellij.lang.annotations.Language;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

@NoArgsConstructor(access = PRIVATE)
public abstract class GroovyXmlUtils {

    //#region parseXmlToGroovyNode()

    @SneakyThrows
    public static Node parseXmlToGroovyNode(Path path) {
        path = normalizePath(path);
        try (val inputStream = newInputStream(path)) {
            val inputSource = new InputSource(inputStream);
            inputSource.setSystemId(path.toString());
            return invokeMethod(newNonValidatingXmlParser(), Node.class, "parse",
                InputSource.class, inputSource
            );
        }
    }

    public static Node parseXmlToGroovyNode(File file) {
        return parseXmlToGroovyNode(file.toPath());
    }

    public static Node parseXmlToGroovyNode(@Language("XML") String content, @Nullable String systemId) {
        val inputSource = new InputSource(new StringReader(content));
        inputSource.setSystemId(systemId);
        return invokeMethod(newNonValidatingXmlParser(), Node.class, "parse",
            InputSource.class, inputSource
        );
    }

    public static Node parseXmlToGroovyNode(@Language("XML") String content) {
        return parseXmlToGroovyNode(content, null);
    }


    private static final Class<?> XML_PARSER_CLASS = getXmlParserClass();

    private static Class<?> getXmlParserClass() {
        List<Throwable> exceptions = new ArrayList<>();

        try {
            return Class.forName("groovy.xml.XmlParser", true, GroovyXmlUtils.class.getClassLoader());
        } catch (Throwable e) {
            exceptions.add(e);
        }

        try {
            return Class.forName("groovy.util.XmlParser", true, GroovyXmlUtils.class.getClassLoader());
        } catch (Throwable e) {
            exceptions.add(e);
        }

        val exception = new IllegalStateException("Groovy's XmlParser class can't be found");
        exceptions.forEach(exception::addSuppressed);
        throw exception;
    }

    private static final Constructor<?> XML_PARSER_CONSTRUCTOR;

    static {
        try {
            XML_PARSER_CONSTRUCTOR = XML_PARSER_CLASS.getConstructor(boolean.class, boolean.class, boolean.class);
        } catch (NoSuchMethodException e) {
            throw sneakyThrow(e);
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static Object newNonValidatingXmlParser() {
        val xmlParser = XML_PARSER_CONSTRUCTOR.newInstance(false, true, true);

        getOptionalMethod(
            (Class<Object>) xmlParser.getClass(),
            "setEntityResolver",
            EntityResolver.class
        ).ifPresent(method ->
            method.invoke(xmlParser, (publicId, systemId) -> new InputSource(new StringReader("")))
        );

        return xmlParser;
    }

    //#endregion


    //#region *GroovyXmlString()

    @Language("XML")
    public static String compactGroovyXmlString(Node node) {
        val stringWriter = new StringWriter();
        val indentPrinter = new IndentPrinter(stringWriter, "", false, false);
        val xmlNodePrinter = newXmlNodePrinter(indentPrinter);
        invokeMethod(xmlNodePrinter, "print",
            Node.class, node
        );
        return stringWriter.toString();
    }

    @Language("XML")
    public static String prettyGroovyXmlString(Node node) {
        return prettyGroovyXmlString(node, DEFAULT_XML_FORMAT);
    }

    @Language("XML")
    public static String prettyGroovyXmlString(Node node, XmlFormat format) {
        val stringWriter = new StringWriter();
        val indentPrinter = new XmlFormatIndentPrinter(stringWriter, format);
        val xmlNodePrinter = newXmlNodePrinter(indentPrinter);
        invokeMethod(xmlNodePrinter, "print",
            Node.class, node
        );
        return stringWriter.toString();
    }


    private static final Class<?> XML_NODE_PRINTER_CLASS = getXmlNodePrinterClass();

    private static Class<?> getXmlNodePrinterClass() {
        List<Throwable> exceptions = new ArrayList<>();

        try {
            return Class.forName("groovy.xml.XmlNodePrinter", true, GroovyXmlUtils.class.getClassLoader());
        } catch (Throwable e) {
            exceptions.add(e);
        }

        try {
            return Class.forName("groovy.util.XmlNodePrinter", true, GroovyXmlUtils.class.getClassLoader());
        } catch (Throwable e) {
            exceptions.add(e);
        }

        val exception = new IllegalStateException("Groovy's XmlNodePrinter class can't be found");
        exceptions.forEach(exception::addSuppressed);
        throw exception;
    }

    private static final Constructor<?> XML_NODE_PRINTER_CONSTRUCTOR;

    static {
        try {
            XML_NODE_PRINTER_CONSTRUCTOR = XML_NODE_PRINTER_CLASS.getConstructor(IndentPrinter.class);
        } catch (NoSuchMethodException e) {
            throw sneakyThrow(e);
        }
    }

    @SneakyThrows
    private static Object newXmlNodePrinter(IndentPrinter indentPrinter) {
        return XML_NODE_PRINTER_CONSTRUCTOR.newInstance(indentPrinter);
    }

    private static class XmlFormatIndentPrinter extends IndentPrinter {

        protected final Writer writer;
        protected final XmlFormat xmlFormat;

        public XmlFormatIndentPrinter(Writer writer, XmlFormat xmlFormat) {
            super(writer, xmlFormat.getIndent());
            this.writer = writer;
            this.xmlFormat = xmlFormat;
        }

        @Override
        @SneakyThrows
        public void println() {
            writer.write(xmlFormat.getLineSeparator());
        }
    }

    //#endregion

}
