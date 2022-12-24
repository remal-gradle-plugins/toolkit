package name.remal.gradle_plugins.toolkit.issues;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface IssuesParser {

    List<Issue> parseIssuesFrom(Path path);

    default List<Issue> parseIssuesFrom(File file) {
        return parseIssuesFrom(file.toPath());
    }

}
