package name.remal.gradle_plugins.toolkit.issues;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static name.remal.gradle_plugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradle_plugins.toolkit.issues.IssueSeverity.WARNING;
import static name.remal.gradle_plugins.toolkit.issues.TextMessage.textMessageOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import org.junit.jupiter.api.Test;

class TextIssuesRendererTest {

    private final TextIssuesRenderer renderer = new TextIssuesRenderer();

    @Test
    void minimal() {
        var issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .build();
        assertEquals(
            format("%s: message", issue.getSourceFile().getAbsolutePath()),
            renderer.renderIssues(singletonList(issue))
        );
    }

    @Test
    @SuppressWarnings("java:S3457")
    void full() {
        var issue = newIssueBuilder()
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
                "WARNING: [category | rule] %s:12:23: message\n\n  description",
                issue.getSourceFile().getAbsolutePath()
            ),
            renderer.renderIssues(singletonList(issue))
        );
    }

}
