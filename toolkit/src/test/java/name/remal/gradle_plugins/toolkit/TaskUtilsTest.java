package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.MaxTestableGradleVersion;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableGradleVersion;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInputsInternal;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class TaskUtilsTest {

    final Project project;

    @Test
    @MinTestableGradleVersion("7.6")
    void onlyIfWithReason() {
        var task = mock(Task.class);
        Spec<Task> spec = it -> true;
        TaskUtils.onlyIfWithReason(task, "reason", spec);
        verify(task).onlyIf("reason", spec);
        verifyNoMoreInteractions(task);
    }

    @Test
    @MaxTestableGradleVersion("7.5.9999")
    void onlyIfWithReason_default() {
        var task = mock(Task.class);
        Spec<Task> spec = it -> true;
        TaskUtils.onlyIfWithReason(task, "reason", spec);
        verify(task).onlyIf(spec);
        verifyNoMoreInteractions(task);
    }

    @Test
    @MinTestableGradleVersion("7.4")
    void markAsNotCompatibleWithConfigurationCache() {
        var task = project.getTasks().register("testTask", DefaultTask.class).get();
        assertTrue(task.isCompatibleWithConfigurationCache());
        TaskUtils.markAsNotCompatibleWithConfigurationCache(task);
        assertFalse(task.isCompatibleWithConfigurationCache());
    }

    @Test
    void clearRegisteredFileProperties() {
        var task = project.getTasks().register("testTask").get();
        var taskInputs = (TaskInputsInternal) task.getInputs();

        taskInputs.dir(project.getProjectDir());

        assertDoesNotThrow(() -> TaskUtils.clearRegisteredFileProperties(taskInputs, true));
    }

    @Test
    void isTaskConfigurable() {
        var task = project.getTasks().register("testTask").get();
        assertTrue(TaskUtils.isTaskConfigurable(task));
    }

}
