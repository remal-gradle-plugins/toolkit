package name.remal.gradleplugins.toolkit.issues;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface IssuesParser {

    List<Issue> parseIssuesFrom(Path path);

    default List<Issue> parseIssuesFrom(File file) {
        return parseIssuesFrom(file.toPath());
    }

}
