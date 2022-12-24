package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.internal.TaskInputsInternal;
import org.gradle.api.internal.tasks.properties.PropertyVisitor;
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

        BooleanSupplier hasFileProperties = () -> {
            val result = new AtomicBoolean(false);
            val visitor = mock(PropertyVisitor.class, invocation -> {
                // visitInputFileProperty method has different signatures in different Gradle versions, so let's just
                // check the invocation's method name
                if (invocation.getMethod().getName().equals("visitInputFileProperty")) {
                    result.set(true);
                }
                return RETURNS_DEFAULTS.answer(invocation);
            });
            taskInputs.visitRegisteredProperties(visitor);
            return result.get();
        };

        assertFalse(hasFileProperties.getAsBoolean());

        taskInputs.dir(project.getProjectDir());
        assertTrue(hasFileProperties.getAsBoolean());

        TaskUtils.clearRegisteredFileProperties(taskInputs);
        assertFalse(hasFileProperties.getAsBoolean());
    }

}
