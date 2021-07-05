package name.remal.gradleplugins.toolkit.issues;

import static java.util.stream.Collectors.groupingBy;
import static name.remal.gradleplugins.toolkit.issues.Utils.ifPresent;
import static name.remal.gradleplugins.toolkit.issues.Utils.streamIssues;
import static org.jdom2.output.Format.getPrettyFormat;
import static org.jdom2.output.LineSeparator.UNIX;

import lombok.val;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

public class CheckstyleXmlIssuesRenderer implements IssuesRenderer {

    private static final XMLOutputter XML_OUTPUTTER = new XMLOutputter(
        getPrettyFormat()
            .setIndent("  ")
            .setEncoding("UTF-8")
            .setOmitDeclaration(true)
            .setOmitEncoding(true)
            .setLineSeparator(UNIX)
    );

    @Override
    public String renderIssues(Iterable<? extends Issue> issues) {
        val document = new Document();

        val checkstyleNode = new Element("checkstyle");
        document.setRootElement(checkstyleNode);

        streamIssues(issues)
            .collect(groupingBy(Issue::getSourceFile))
            .forEach((sourceFile, fileIssues) -> {
                val fileNode = new Element("file");
                checkstyleNode.addContent(fileNode);

                fileNode.setAttribute("name", sourceFile.getPath());

                fileIssues.forEach(issue -> {
                    val errorNode = new Element("error");
                    fileNode.addContent(errorNode);

                    val severity = issue.getSeverity();
                    if (severity == IssueSeverity.ERROR) {
                        errorNode.setAttribute("severity", "error");
                    } else if (severity == IssueSeverity.WARNING) {
                        errorNode.setAttribute("severity", "warning");
                    } else if (severity == IssueSeverity.INFO) {
                        errorNode.setAttribute("severity", "info");
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
