package name.remal.gradleplugins.toolkit.testkit;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.ProjectStateInternal;
import org.gradle.configuration.project.LifecycleProjectEvaluator;

public interface ProjectAfterEvaluateActionsExecutor {

    /**
     * See {@link LifecycleProjectEvaluator#evaluate}
     */
    static void executeAfterEvaluateActions(Project project) {
        val stateInternal = (ProjectStateInternal) project.getState();
        val projectInternal = (ProjectInternal) project;
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

    static void executeAfterEvaluateActionsForAllProjects(Project project) {
        project.allprojects(ProjectAfterEvaluateActionsExecutor::executeAfterEvaluateActions);
    }

}
