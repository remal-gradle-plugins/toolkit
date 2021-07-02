package name.remal.gradleplugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.internal.TaskInputsInternal;
import org.gradle.api.internal.tasks.properties.PropertyVisitor;
import org.junit.jupiter.api.Test;

class TaskUtilsTest {

    @Test
    void clearRegisteredFileProperties(Project project) {
        val task = project.getTasks().create("testTask");
        val taskInputs = (TaskInputsInternal) task.getInputs();

        BooleanSupplier hasFileProperties = () -> {
            val result = new AtomicBoolean(false);
            val visitor = mock(PropertyVisitor.class, invocation -> {
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
