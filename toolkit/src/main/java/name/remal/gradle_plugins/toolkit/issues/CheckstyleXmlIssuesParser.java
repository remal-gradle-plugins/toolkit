package name.remal.gradle_plugins.toolkit.issues;

import static java.nio.file.Files.newInputStream;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.internal.JdomUtils.newNonValidatingSaxBuilder;
import static name.remal.gradle_plugins.toolkit.internal.JdomUtils.withoutNamespaces;
import static name.remal.gradle_plugins.toolkit.issues.CheckstyleXmlUtils.getIssueSeverityFor;
import static name.remal.gradle_plugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradle_plugins.toolkit.issues.TextMessage.textMessageOf;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.ObjectUtils;
import org.jdom2.Document;
import org.jetbrains.annotations.Contract;

public class CheckstyleXmlIssuesParser implements IssuesParser {

    @Override
    @SneakyThrows
    public List<Issue> parseIssuesFrom(Path path) {
        path = normalizePath(path);

        final Document document;
        try (var inputStream = newInputStream(path)) {
            document = withoutNamespaces(
                newNonValidatingSaxBuilder().build(inputStream, path.toString())
            );
        }

        List<Issue> issues = new ArrayList<>();

        for (var fileElement : document.getRootElement().getChildren("file")) {
            var file = fileElement.getAttributeValue("name");
            if (file == null) {
                continue;
            }

            for (var errorElement : fileElement.getChildren("error")) {
                var message = errorElement.getAttributeValue("message");
                if (isEmpty(message)) {
                    continue;
                }

                var issueBuilder = newIssueBuilder();
                issueBuilder.sourceFile(new File(file));
                issueBuilder.message(textMessageOf(message));

                issueBuilder.severity(getIssueSeverityFor(errorElement.getAttributeValue("severity")));

                issueBuilder.startLine(getIntegerFor(errorElement.getAttributeValue("line")));
                issueBuilder.startColumn(getIntegerFor(errorElement.getAttributeValue("column")));

                Optional.ofNullable(errorElement.getAttributeValue("source"))
                    .filter(ObjectUtils::isNotEmpty)
                    .ifPresent(issueBuilder::rule);

                issues.add(issueBuilder.build());
            }
        }

        return issues;
    }

    @Nullable
    @Contract("null->null")
    private static Integer getIntegerFor(@Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

}
