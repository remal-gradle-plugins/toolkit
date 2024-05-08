package name.remal.gradle_plugins.toolkit.issues;

import static java.util.Collections.singletonList;
import static name.remal.gradle_plugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradle_plugins.toolkit.issues.TextMessage.textMessageOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import lombok.val;
import name.remal.gradle_plugins.toolkit.TagXslt;
import org.junit.jupiter.api.Test;

@TagXslt
class CheckstyleHtmlIssuesRendererTest {

    private final CheckstyleHtmlIssuesRenderer renderer = new CheckstyleHtmlIssuesRenderer();

    @Test
    void does_not_throw_exception() {
        val issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .build();
        assertDoesNotThrow(() -> renderer.renderIssues(singletonList(issue)));
    }

}
