package name.remal.gradleplugins.toolkit.issues;

import static java.util.Collections.singletonList;
import static name.remal.gradleplugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradleplugins.toolkit.issues.TextMessage.textMessageOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import lombok.val;
import org.junit.jupiter.api.Test;

class CheckstyleHtmlIssuesRendererTest {

    private final CheckstyleXmlIssuesRenderer renderer = new CheckstyleXmlIssuesRenderer();

    @Test
    void does_not_throw_exception() {
        val issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .build();
        assertDoesNotThrow(() -> renderer.renderIssues(singletonList(issue)));
    }

}
