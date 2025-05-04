package name.remal.gradle_plugins.toolkit.issues;

import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.JavaSerializationUtils.deserializeFrom;
import static name.remal.gradle_plugins.toolkit.JavaSerializationUtils.serializeToBytes;
import static name.remal.gradle_plugins.toolkit.issues.HtmlMessage.htmlMessageOf;
import static name.remal.gradle_plugins.toolkit.issues.Issue.newIssueBuilder;
import static name.remal.gradle_plugins.toolkit.issues.IssueSeverity.INFO;
import static name.remal.gradle_plugins.toolkit.issues.TextMessage.textMessageOf;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.File;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class IssueTest {

    @Test
    void serialization() {
        var issue = newIssueBuilder()
            .sourceFile(new File("."))
            .message(textMessageOf("message"))
            .severity(INFO)
            .rule("rule")
            .category("category")
            .startLine(1)
            .startColumn(11)
            .endLine(2)
            .endColumn(12)
            .consistentId("consistentId")
            .description(htmlMessageOf("<p>description"))
            .build();
        var bytes = serializeToBytes(issue);
        var deserialized = deserializeFrom(bytes, Issue.class);
        assertThat(deserialized)
            .isEqualTo(issue);
    }

    @Test
    void json() {
        var gsonBuilder = new GsonBuilder()
            .setPrettyPrinting();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        var gson = gsonBuilder.create();

        var issue = newIssueBuilder()
            .sourceFile(normalizeFile(new File(".")))
            .message(textMessageOf("message"))
            .severity(INFO)
            .rule("rule")
            .category("category")
            .startLine(1)
            .startColumn(11)
            .endLine(2)
            .endColumn(12)
            .consistentId("consistentId")
            .description(htmlMessageOf("<p>description"))
            .build();
        var json = gson.toJson(issue);
        var deserialized = gson.fromJson(json, Issue.class);
        assertThat(deserialized)
            .isEqualTo(issue);
    }

}
