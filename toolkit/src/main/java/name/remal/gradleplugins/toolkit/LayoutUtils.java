package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.ProjectUtils.getTopLevelDirOf;
import static name.remal.gradleplugins.toolkit.git.GitUtils.findGitRepositoryRootFor;

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

        return topLevelDir;
    }

    public static File getRootDirOf(Project project) {
        return getRootPathOf(project).toFile();
    }

}
