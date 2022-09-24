package name.remal.gradleplugins.toolkit.issues;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;

import java.io.File;
import lombok.SneakyThrows;
import lombok.val;

public interface IssuesRenderer {

    String renderIssues(Iterable<? extends Issue> issues);

    @SneakyThrows
    default void renderIssuesToFile(Iterable<? extends Issue> issues, File file) {
        file = file.getAbsoluteFile();
        val parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.equals(file)) {
            createDirectories(parentFile.toPath());
        }

        val content = renderIssues(issues);
        write(file.toPath(), content.getBytes(UTF_8));
    }

}
