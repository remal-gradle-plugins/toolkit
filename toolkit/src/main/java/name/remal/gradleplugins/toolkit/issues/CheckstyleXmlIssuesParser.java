package name.remal.gradleplugins.toolkit.issues;

import static java.nio.file.Files.newInputStream;
import static name.remal.gradleplugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradleplugins.toolkit.internal.JdomUtils.newNonValidatingSaxBuilder;
import static name.remal.gradleplugins.toolkit.internal.JdomUtils.withoutNamespaces;
import static name.remal.gradleplugins.toolkit.issues.CheckstyleXmlUtils.getIssueSeverityFor;
import static name.remal.gradleplugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradleplugins.toolkit.issues.TextMessage.textMessageOf;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradleplugins.toolkit.ObjectUtils;
import org.jdom2.Document;
import org.jetbrains.annotations.Contract;

public class CheckstyleXmlIssuesParser implements IssuesParser {

    @Override
    @SneakyThrows
    public List<Issue> parseIssuesFrom(Path path) {
        path = normalizePath(path);

        final Document document;
        try (val inputStream = newInputStream(path)) {
            document = withoutNamespaces(
                newNonValidatingSaxBuilder().build(inputStream, path.toString())
            );
        }

        List<Issue> issues = new ArrayList<>();

        for (val fileElement : document.getRootElement().getChildren("file")) {
            val file = fileElement.getAttributeValue("name");
            if (file == null) {
                continue;
            }

            for (val errorElement : fileElement.getChildren("error")) {
                val message = errorElement.getAttributeValue("message");
                if (isEmpty(message)) {
                    continue;
                }

                val issueBuilder = newIssueBuilder();
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
