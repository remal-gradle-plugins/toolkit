package name.remal.gradleplugins.toolkit;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptyList;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.makeAccessible;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskInputs;
import org.jetbrains.annotations.VisibleForTesting;

public abstract class TaskUtils {

    public static boolean isInTaskGraph(Task task) {
        return task.getProject()
            .getGradle()
            .getTaskGraph()
            .hasTask(task);
    }

    public static boolean isRequested(Task task) {
        return task.getProject()
            .getGradle()
            .getStartParameter()
            .getTaskNames()
            .stream()
            .anyMatch(task.getName()::equals);
    }

    public static void disableTask(Task task) {
        task.setEnabled(false);
        task.onlyIf(__ -> false);
        task.setDependsOn(emptyList());
        clearRegisteredFileProperties(task.getInputs());
    }

    @VisibleForTesting
    @SneakyThrows
    static void clearRegisteredFileProperties(TaskInputs taskInputs) {
        Class<?> taskInputsType = unwrapGeneratedSubclass(taskInputs.getClass());
        while (taskInputsType != Object.class) {
            try {
                val field = taskInputsType.getDeclaredField("registeredFileProperties");
                if (!isStatic(field.getModifiers()) && Iterable.class.isAssignableFrom(field.getType())) {
                    val properties = (Iterable<?>) makeAccessible(field).get(taskInputs);
                    val iterator = properties.iterator();
                    while (iterator.hasNext()) {
                        iterator.next();
                        iterator.remove();
                    }
                    break;
                }
            } catch (NoSuchFieldException e) {
                // do nothing
            }
            taskInputsType = taskInputsType.getSuperclass();
        }
    }

    private TaskUtils() {
    }

}
