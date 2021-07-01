package name.remal.gradleplugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.internal.TaskInputsInternal;
import org.gradle.api.internal.tasks.properties.InputFilePropertyType;
import org.gradle.api.internal.tasks.properties.PropertyValue;
import org.gradle.api.internal.tasks.properties.PropertyVisitor;
import org.gradle.api.tasks.FileNormalizer;
import org.gradle.internal.fingerprint.DirectorySensitivity;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

class TaskUtilsTest {

    @Test
    void clearRegisteredFileProperties(Project project) {
        val task = project.getTasks().create("testTask");
        val taskInputs = (TaskInputsInternal) task.getInputs();

        BooleanSupplier hasFileProperties = () -> {
            val result = new AtomicBoolean(false);
            taskInputs.visitRegisteredProperties(new PropertyVisitor.Adapter() {
                @Override
                public void visitInputFileProperty(
                    String propertyName,
                    boolean optional,
                    boolean skipWhenEmpty,
                    DirectorySensitivity directorySensitivity,
                    boolean incremental,
                    @Nullable Class<? extends FileNormalizer> fileNormalizer,
                    PropertyValue value,
                    InputFilePropertyType filePropertyType
                ) {
                    result.set(true);
                }
            });
            return result.get();
        };

        assertFalse(hasFileProperties.getAsBoolean());

        taskInputs.dir(project.getProjectDir());
        assertTrue(hasFileProperties.getAsBoolean());

        TaskUtils.clearRegisteredFileProperties(taskInputs);
        assertFalse(hasFileProperties.getAsBoolean());
    }

}
