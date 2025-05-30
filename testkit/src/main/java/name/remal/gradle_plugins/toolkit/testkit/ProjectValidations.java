package name.remal.gradle_plugins.toolkit.testkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.ProjectStateInternal;
import org.gradle.configuration.project.LifecycleProjectEvaluator;

@ReliesOnInternalGradleApi
@NoArgsConstructor(access = PRIVATE)
public abstract class ProjectValidations {

    /**
     * See {@link LifecycleProjectEvaluator#evaluate}
     */
    public static void executeAfterEvaluateActions(Project project) {
        var stateInternal = (ProjectStateInternal) project.getState();
        var projectInternal = (ProjectInternal) project;
        ProjectEvaluationListener nextBatch = projectInternal.getProjectEvaluationBroadcaster();
        Action<ProjectEvaluationListener> fireAction = listener -> {
            listener.afterEvaluate(projectInternal, stateInternal);
        };

        if (stateInternal.getExecuted()) {
            throw new IllegalStateException("Project has already been executed: " + project);
        }

        stateInternal.toAfterEvaluate();
        try {
            do {
                nextBatch = projectInternal.stepEvaluationListener(nextBatch, fireAction);
            } while (nextBatch != null);
        } finally {
            stateInternal.configured();
        }
    }

    public static void executeAfterEvaluateActionsForAllProjects(Project project) {
        project.allprojects(ProjectValidations::executeAfterEvaluateActions);
    }

}
