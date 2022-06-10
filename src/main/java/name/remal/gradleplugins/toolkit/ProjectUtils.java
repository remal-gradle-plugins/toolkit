package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Project;

@NoArgsConstructor(access = PRIVATE)
public abstract class ProjectUtils {

    public static void afterEvaluateOrNow(Project project, Action<? super Project> action) {
        if (project.getState().getExecuted()) {
            action.execute(project);
        } else {
            project.afterEvaluate(action);
        }
    }

}
