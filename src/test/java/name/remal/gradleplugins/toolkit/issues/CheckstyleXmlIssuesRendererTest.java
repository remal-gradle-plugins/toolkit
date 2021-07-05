package name.remal.gradleplugins.toolkit.issues;

import static java.util.Collections.singletonList;
import static name.remal.gradleplugins.toolkit.StringUtils.escapeXml;
import static name.remal.gradleplugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradleplugins.toolkit.issues.IssueSeverity.WARNING;
import static name.remal.gradleplugins.toolkit.issues.TextMessage.textMessageOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import lombok.val;
import org.junit.jupiter.api.Test;

class CheckstyleXmlIssuesRendererTest {

    private final CheckstyleXmlIssuesRenderer renderer = new CheckstyleXmlIssuesRenderer();

    @Test
    void minimal() {
        val issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .build();
        assertEquals(
            "<checkstyle>\n"
                + "  <file name=\"" + escapeXml(issue.getSourceFile().getPath()) + "\">\n"
                + "    <error message=\"message\" />\n"
                + "  </file>\n"
                + "</checkstyle>\n",
            renderer.renderIssues(singletonList(issue))
        );

    }

    @Test
    void full() {
        val issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .severity(WARNING)
            .rule("rule")
            .category("category")
            .startLine(12)
            .startColumn(23)
            .endLine(112)
            .endColumn(123)
            .description(textMessageOf("description"))
            .build();
        assertEquals(
            "<checkstyle>\n"
                + "  <file name=\"" + escapeXml(issue.getSourceFile().getPath()) + "\">\n"
                + "    <error severity=\"warning\" line=\"12\" column=\"23\" message=\"message\" source=\"rule\" />\n"
                + "  </file>\n"
                + "</checkstyle>\n",
            renderer.renderIssues(singletonList(issue))
        );
    }

}
