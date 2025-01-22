package name.remal.gradle_plugins.toolkit.issues;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.StringUtils.indentString;
import static name.remal.gradle_plugins.toolkit.StringUtils.normalizeString;
import static name.remal.gradle_plugins.toolkit.issues.Utils.appendDelimiter;
import static name.remal.gradle_plugins.toolkit.issues.Utils.ifPresent;
import static name.remal.gradle_plugins.toolkit.issues.Utils.streamIssues;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import name.remal.gradle_plugins.toolkit.ObjectUtils;
import org.intellij.lang.annotations.Language;

@NoArgsConstructor
@Getter
@With
@AllArgsConstructor(access = PRIVATE)
public class TextIssuesRenderer implements IssuesRenderer {

    private boolean description = true;

    @Language("TEXT")
    @Override
    @SuppressWarnings("java:S3776")
    public String renderIssues(Iterable<? extends Issue> issues) {
        return streamIssues(issues)
            .map(issue -> {
                var sb = new StringBuilder();

                ifPresent(issue.getSeverity(), it -> sb.append(it).append(':'));

                var ruleText = Stream.of(issue.getCategory(), issue.getRule())
                    .filter(ObjectUtils::isNotEmpty)
                    .collect(joining(" | "));
                if (!ruleText.isEmpty()) {
                    appendDelimiter(sb).append('[').append(ruleText).append(']');
                }

                appendDelimiter(sb).append(issue.getSourceFile().getAbsolutePath());

                ifPresent(issue.getStartLine(), it -> sb.append(':').append(it));
                ifPresent(issue.getStartColumn(), it -> sb.append(':').append(it));

                var messageText = messageToText(issue.getMessage());
                if (!messageText.isEmpty()) {
                    if (messageText.contains("\n")) {
                        sb.append('\n').append(indentString(messageText));
                    } else {
                        sb.append(": ").append(messageText);
                    }
                }

                if (description) {
                    var descriptionText = messageToText(issue.getDescription());
                    if (!descriptionText.isEmpty()) {
                        if (!messageText.isEmpty()) {
                            sb.append('\n');
                        }
                        sb.append('\n').append(indentString(descriptionText));
                    }
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
