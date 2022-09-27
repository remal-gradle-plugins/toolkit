package name.remal.gradleplugins.toolkit.issues;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static name.remal.gradleplugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;

import java.io.File;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.val;

public interface IssuesRenderer {

    String renderIssues(Iterable<? extends Issue> issues);

    @SneakyThrows
    default void renderIssuesToPath(Iterable<? extends Issue> issues, Path path) {
        val content = renderIssues(issues);
        path = normalizePath(path);
        createParentDirectories(path);
        write(path, content.getBytes(UTF_8));
    }

    default void renderIssuesToFile(Iterable<? extends Issue> issues, File file) {
        renderIssuesToPath(issues, file.toPath());
    }

}
