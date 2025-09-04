package name.remal.gradle_plugins.toolkit.issues;

import static java.nio.file.Files.writeString;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import java.io.File;
import java.nio.file.Path;
import lombok.SneakyThrows;

public interface IssuesRenderer {

    String renderIssues(Iterable<? extends Issue> issues);

    @SneakyThrows
    default void renderIssuesToPath(Iterable<? extends Issue> issues, Path path) {
        var content = renderIssues(issues);
        path = normalizePath(path);
        createParentDirectories(path);
        writeString(path, content);
    }

    default void renderIssuesToFile(Iterable<? extends Issue> issues, File file) {
        renderIssuesToPath(issues, file.toPath());
    }

}
