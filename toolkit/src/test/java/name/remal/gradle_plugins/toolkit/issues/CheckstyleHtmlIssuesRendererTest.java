package name.remal.gradle_plugins.toolkit.issues;

import static java.util.Collections.singletonList;
import static name.remal.gradle_plugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradle_plugins.toolkit.issues.TextMessage.textMessageOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import lombok.val;
import name.remal.gradle_plugins.toolkit.TagXslt;
import org.junit.jupiter.api.Test;

@TagXslt
class CheckstyleHtmlIssuesRendererTest {

    private static final String TOOL_NAME = "asdiuhAZDXUHZADklhjcAd";
    private static final String RULE_NAME = "XxkajdSSaz";

    private final CheckstyleHtmlIssuesRenderer renderer = new CheckstyleHtmlIssuesRenderer(TOOL_NAME);

    @Test
    void does_not_throw_exception() {
        val issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .build();
        assertDoesNotThrow(() -> renderer.renderIssues(singletonList(issue)));
    }

    @Test
    void does_not_include_word_checkstyle() {
        val issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .build();
        val renderedContent = renderer.renderIssues(singletonList(issue));
        assertThat(renderedContent)
            .doesNotContainIgnoringCase("checkstyle");
    }

    @Test
    void includes_tool_name() {
        val issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .build();
        val renderedContent = renderer.renderIssues(singletonList(issue));
        assertThat(renderedContent)
            .contains(TOOL_NAME);
    }

    @Test
    void includes_rule() {
        val issue = newIssueBuilder()
            .sourceFile(new File("source"))
            .message(textMessageOf("message"))
            .rule(RULE_NAME)
            .build();
        val renderedContent = renderer.renderIssues(singletonList(issue));
        assertThat(renderedContent)
            .matches("[\\s\\S]*<th[^>]*>\\s*Rule\\s*</th>[\\s\\S]*")
            .contains(RULE_NAME);
    }

}
