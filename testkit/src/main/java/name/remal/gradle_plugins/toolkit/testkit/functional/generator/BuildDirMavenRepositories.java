package name.remal.gradle_plugins.toolkit.testkit.functional.generator;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;

import com.google.common.base.Splitter;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.PathUtils;
import org.jetbrains.annotations.Unmodifiable;

@NoArgsConstructor(access = PRIVATE)
public abstract class BuildDirMavenRepositories {

    private static final String BUILD_DIR_MAVEN_REPOSITORIES_PATHS_PROPERTY = "build-dir-maven-repos";

    @Unmodifiable
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
            .collect(toImmutableList());
    }

}