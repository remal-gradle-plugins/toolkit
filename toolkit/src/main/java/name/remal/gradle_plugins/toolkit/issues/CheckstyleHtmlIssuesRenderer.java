package name.remal.gradle_plugins.toolkit.issues;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static name.remal.gradle_plugins.toolkit.ResourceUtils.getResourceUrl;
import static name.remal.gradle_plugins.toolkit.UrlUtils.openInputStreamForUrl;
import static name.remal.gradle_plugins.toolkit.xml.XmlUtils.tryToSetXmlSetting;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.SneakyThrows;
import org.gradle.util.GradleVersion;

public class CheckstyleHtmlIssuesRenderer implements IssuesRenderer {

    private static final CheckstyleXmlIssuesRenderer XML_RENDERER = new CheckstyleXmlIssuesRenderer();


    private final String toolName;

    public CheckstyleHtmlIssuesRenderer(String toolName) {
        this.toolName = toolName;
    }

    public CheckstyleHtmlIssuesRenderer() {
        this("");
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("java:S2755")
    public String renderIssues(Iterable<? extends Issue> issues) {
        var xsltUrl = getResourceUrl("checkstyle.xsl", CheckstyleHtmlIssuesRenderer.class);
        try (var xsltInputStream = openInputStreamForUrl(xsltUrl)) {
            var xslt = new StreamSource(xsltInputStream, xsltUrl.toString());

            var factory = TransformerFactory.newInstance();
            tryToSetXmlSetting(factory, ACCESS_EXTERNAL_DTD, "");
            tryToSetXmlSetting(factory, ACCESS_EXTERNAL_STYLESHEET, "");
            tryToSetXmlSetting(factory, FEATURE_SECURE_PROCESSING, true);

            var transformer = factory.newTransformer(xslt);
            transformer.setParameter("toolName", toolName);
            transformer.setParameter("gradleVersion", GradleVersion.current().getVersion());

            var xmlContent = XML_RENDERER.renderIssues(issues);
            var source = new StreamSource(new StringReader(xmlContent));
            var outputWriter = new StringWriter();
            transformer.transform(source, new StreamResult(outputWriter));

            return outputWriter.toString();
        }
    }

}
