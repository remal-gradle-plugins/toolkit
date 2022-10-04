package name.remal.gradleplugins.toolkit.issues;

import static java.util.stream.Collectors.joining;
import static name.remal.gradleplugins.toolkit.StringUtils.indentString;
import static name.remal.gradleplugins.toolkit.StringUtils.normalizeString;
import static name.remal.gradleplugins.toolkit.issues.Utils.appendDelimiter;
import static name.remal.gradleplugins.toolkit.issues.Utils.ifPresent;
import static name.remal.gradleplugins.toolkit.issues.Utils.streamIssues;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.val;
import org.intellij.lang.annotations.Language;

public class TextIssuesRenderer implements IssuesRenderer {

    @Language("TEXT")
    @Override
    public String renderIssues(Iterable<? extends Issue> issues) {
        return streamIssues(issues)
            .map(issue -> {
                val sb = new StringBuilder();

                ifPresent(issue.getSeverity(), it -> sb.append(it).append(':'));

                val ruleText = Stream.of(issue.getCategory(), issue.getRule())
                    .filter(Utils::isNotEmpty)
                    .collect(joining(" | "));
                if (!ruleText.isEmpty()) {
                    appendDelimiter(sb).append('[').append(ruleText).append(']');
                }

                appendDelimiter(sb).append(issue.getSourceFile().getAbsolutePath());

                ifPresent(issue.getStartLine(), it -> sb.append(':').append(it));
                ifPresent(issue.getStartColumn(), it -> sb.append(':').append(it));

                val messageText = messageToText(issue.getMessage());
                if (!messageText.isEmpty()) {
                    if (messageText.contains("\n")) {
                        sb.append('\n').append(indentString(messageText));
                    } else {
                        sb.append(": ").append(messageText);
                    }
                }

                val descriptionText = messageToText(issue.getDescription());
                if (!descriptionText.isEmpty()) {
                    if (!messageText.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append('\n').append(indentString(descriptionText));
                }

                return sb.toString();
            })
            .collect(joining("\n\n"));
    }

    @Language("TEXT")
    private static String messageToText(@Nullable Message message) {
        return message != null ? normalizeString(message.renderAsText()) : "";
    }

}
