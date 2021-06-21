package name.remal.gradleplugins.toolkit.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ProjectAfterEvaluateActionsExecutorTest {

    private final Project project;

    @Test
    void executeAfterEvaluateActions() {
        val evaluatedProject = new AtomicReference<Project>();
        project.afterEvaluate(evaluatedProject::set);
        assertNull(evaluatedProject.get());

        ProjectAfterEvaluateActionsExecutor.executeAfterEvaluateActions(project);
        assertEquals(project, evaluatedProject.get());

        assertThrows(IllegalStateException.class, () ->
            ProjectAfterEvaluateActionsExecutor.executeAfterEvaluateActions(project)
        );
    }

}
