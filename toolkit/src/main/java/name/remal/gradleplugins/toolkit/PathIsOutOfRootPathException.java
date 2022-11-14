package name.remal.gradleplugins.toolkit;

import static java.lang.String.format;

import java.nio.file.Path;

public class PathIsOutOfRootPathException extends RuntimeException {

    PathIsOutOfRootPathException(Path path, Path rootPath) {
        super(format(
            "Path is outside of root path (%s): %s",
            rootPath,
            path
        ));
    }

}
