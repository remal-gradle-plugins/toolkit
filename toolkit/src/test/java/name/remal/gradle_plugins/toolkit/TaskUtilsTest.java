package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.internal.TaskInputsInternal;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class TaskUtilsTest {

    private final Project project;

    @Test
    @MinSupportedGradleVersion("7.4")
    void markAsNotCompatibleWithConfigurationCache() {
        val task = project.getTasks().create("testTask", DefaultTask.class);
        assertTrue(task.isCompatibleWithConfigurationCache());
        TaskUtils.markAsNotCompatibleWithConfigurationCache(task);
        assertFalse(task.isCompatibleWithConfigurationCache());
    }

    @Test
    void clearRegisteredFileProperties() {
        val task = project.getTasks().create("testTask");
        val taskInputs = (TaskInputsInternal) task.getInputs();

        taskInputs.dir(project.getProjectDir());

        assertDoesNotThrow(() -> TaskUtils.clearRegisteredFileProperties(taskInputs, true));
    }

}
