package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;

import java.nio.file.Path;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;

@NoArgsConstructor(access = PRIVATE)
public abstract class ProjectUtils {

    public static Path getTopLevelDirOf(Project project) {
        val rootProject = project.getRootProject();
        val projectDir = normalizePath(rootProject.getProjectDir().toPath());
        if (isBuildSrcProject(project)) {
            return projectDir.getParent();
        } else {
            return projectDir;
        }
    }

    public static boolean isBuildSrcProject(Project project) {
        project = project.getRootProject();
        return project.getName().equals("buildSrc");
    }

    public static void afterEvaluateOrNow(Project project, Action<? super Project> action) {
        if (project.getState().getExecuted()) {
            action.execute(project);
        } else {
            project.afterEvaluate(action);
        }
    }

}
