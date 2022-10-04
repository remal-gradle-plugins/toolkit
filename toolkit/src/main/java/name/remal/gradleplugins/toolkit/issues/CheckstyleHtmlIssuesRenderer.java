package name.remal.gradleplugins.toolkit.issues;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;
import static name.remal.gradleplugins.toolkit.ResourceUtils.getResourceUrl;
import static name.remal.gradleplugins.toolkit.UrlUtils.openInputStreamForUrl;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.SneakyThrows;
import lombok.val;

public class CheckstyleHtmlIssuesRenderer implements IssuesRenderer {

    private static final CheckstyleXmlIssuesRenderer XML_RENDERER = new CheckstyleXmlIssuesRenderer();

    @Override
    @SneakyThrows
    public String renderIssues(Iterable<? extends Issue> issues) {
        val xsltUrl = getResourceUrl("checkstyle.xsl", CheckstyleHtmlIssuesRenderer.class);
        try (val xsltInputStream = openInputStreamForUrl(xsltUrl)) {
            val xslt = new StreamSource(xsltInputStream, xsltUrl.toString());

            val xmlContent = XML_RENDERER.renderIssues(issues);
            val source = new StreamSource(new StringReader(xmlContent));

            val outputWriter = new StringWriter();

            val factory = TransformerFactory.newInstance();
            tryToSetAttribute(factory, ACCESS_EXTERNAL_DTD, "");
            tryToSetAttribute(factory, ACCESS_EXTERNAL_STYLESHEET, "");

            val transformer = factory.newTransformer(xslt);
            transformer.transform(source, new StreamResult(outputWriter));

            return outputWriter.toString();
        }
    }

    private static void tryToSetAttribute(TransformerFactory factory, String name, Object value) {
        try {
            factory.setAttribute(name, value);
        } catch (IllegalArgumentException ignored) {
            // do nothing
        }
    }

}
