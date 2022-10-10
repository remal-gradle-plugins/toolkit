package name.remal.gradleplugins.toolkit.testkit.functional;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.PredicateUtils.not;

import com.google.common.base.Splitter;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradleplugins.toolkit.PathUtils;

@NoArgsConstructor(access = PRIVATE)
public abstract class BuildDirMavenRepositories {

    private static final String BUILD_DIR_MAVEN_REPOSITORIES_PATHS_PROPERTY = "build-dir-maven-repos";

    @SuppressWarnings("UnstableApiUsage")
    public static Collection<Path> getBuildDirMavenRepositories() {
        val paths = System.getProperty(BUILD_DIR_MAVEN_REPOSITORIES_PATHS_PROPERTY);
        if (paths == null) {
            throw new IllegalStateException(
                "System property is not set: " + BUILD_DIR_MAVEN_REPOSITORIES_PATHS_PROPERTY
            );
        }

        return Splitter.on(File.pathSeparator).splitToStream(paths)
            .map(String::trim)
            .filter(not(String::isEmpty))
            .map(Paths::get)
            .map(PathUtils::normalizePath)
            .distinct()
            .collect(toList());
    }

}
