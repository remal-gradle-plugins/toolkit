package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CiUtils.getCiSystem;
import static name.remal.gradle_plugins.toolkit.ProjectUtils.getTopLevelDirOf;
import static name.remal.gradle_plugins.toolkit.git.GitUtils.findGitRepositoryRootFor;

import java.io.File;
import java.nio.file.Path;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.Project;

@NoArgsConstructor(access = PRIVATE)
public abstract class LayoutUtils {

    public static Path getRootPathOf(Project project) {
        val topLevelDir = getTopLevelDirOf(project);
        val repositoryRoot = findGitRepositoryRootFor(topLevelDir);
        if (repositoryRoot != null) {
            return repositoryRoot;
        }

        val ciBuildDir = getCiSystem()
            .map(CiSystem::getBuildDirIfSupported)
            .orElse(null);
        if (ciBuildDir != null) {
            return ciBuildDir.toPath();
        }

        return topLevelDir;
    }

    public static File getRootDirOf(Project project) {
        return getRootPathOf(project).toFile();
    }

}
