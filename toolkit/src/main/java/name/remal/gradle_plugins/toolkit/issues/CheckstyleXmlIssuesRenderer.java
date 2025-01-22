package name.remal.gradle_plugins.toolkit.issues;

import static java.util.stream.Collectors.groupingBy;
import static name.remal.gradle_plugins.toolkit.issues.CheckstyleXmlUtils.getCheckstyleSeverityFor;
import static name.remal.gradle_plugins.toolkit.issues.Utils.ifPresent;
import static name.remal.gradle_plugins.toolkit.issues.Utils.streamIssues;
import static org.jdom2.output.Format.getPrettyFormat;
import static org.jdom2.output.LineSeparator.UNIX;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

public class CheckstyleXmlIssuesRenderer implements IssuesRenderer {

    private static final XMLOutputter XML_OUTPUTTER = new XMLOutputter(
        getPrettyFormat()
            .setIndent("  ")
            .setEncoding("UTF-8")
            .setOmitDeclaration(false)
            .setOmitEncoding(false)
            .setLineSeparator(UNIX)
    );

    @Override
    public String renderIssues(Iterable<? extends Issue> issues) {
        var document = new Document();

        var checkstyleNode = new Element("checkstyle");
        document.setRootElement(checkstyleNode);

        streamIssues(issues)
            .collect(groupingBy(Issue::getSourceFile))
            .forEach((sourceFile, fileIssues) -> {
                var fileNode = new Element("file");
                checkstyleNode.addContent(fileNode);

                fileNode.setAttribute("name", sourceFile.getPath());

                fileIssues.forEach(issue -> {
                    var errorNode = new Element("error");
                    fileNode.addContent(errorNode);

                    var severity = getCheckstyleSeverityFor(issue.getSeverity());
                    if (severity != null) {
                        errorNode.setAttribute("severity", severity);
                    }

                    ifPresent(issue.getStartLine(), it -> errorNode.setAttribute("line", "" + it));
                    ifPresent(issue.getStartColumn(), it -> errorNode.setAttribute("column", "" + it));

                    errorNode.setAttribute("message", issue.getMessage().renderAsText());

                    ifPresent(issue.getRule(), it -> errorNode.setAttribute("source", it));
                });
            });

        return XML_OUTPUTTER.outputString(document);
    }

}
