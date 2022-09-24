package name.remal.gradleplugins.toolkit.issues;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static name.remal.gradleplugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradleplugins.toolkit.issues.IssueSeverity.WARNING;
import static name.remal.gradleplugins.toolkit.issues.TextMessage.textMessageOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import lombok.val;
import org.junit.jupiter.api.Test;

class TextIssuesRendererTest {

    private final TextIssuesRenderer renderer = new TextIssuesRenderer();

    @Test
    void minimal() {
        val issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .build();
        assertEquals(
            format("%s\n  message", issue.getSourceFile().getAbsolutePath()),
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
            format(
                "WARNING: [category | rule] %s:12:23\n  message\n\n  description",
                issue.getSourceFile().getAbsolutePath()
            ),
            renderer.renderIssues(singletonList(issue))
        );
    }

}
