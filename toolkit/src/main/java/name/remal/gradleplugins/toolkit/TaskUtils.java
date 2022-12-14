package name.remal.gradleplugins.toolkit;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.makeAccessible;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import java.io.File;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradleplugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskInputs;
import org.jetbrains.annotations.VisibleForTesting;

@NoArgsConstructor(access = PRIVATE)
public abstract class TaskUtils {

    /**
     * Execute {@code action} before task cache state is calculated, and any task action is executed.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Task> void doBeforeTaskExecution(T task, Action<? super T> action) {
        task.onlyIf(new DescribableSpec<>("Before task execution", currentTask -> {
            action.execute((T) currentTask);
            return true;
        }));
    }


    private static final String DEFAULT_NOT_COMPATIBLE_WITH_CONFIGURATION_CACHE_REASON =
        "Not yet compatible with Configuration Cache";

    public static void markAsNotCompatibleWithConfigurationCache(Task task) {
        markAsNotCompatibleWithConfigurationCache(task, DEFAULT_NOT_COMPATIBLE_WITH_CONFIGURATION_CACHE_REASON);
    }

    @ReliesOnInternalGradleApi
    public static void markAsNotCompatibleWithConfigurationCache(Task task, String reason) {
        if (isEmpty(reason)) {
            reason = DEFAULT_NOT_COMPATIBLE_WITH_CONFIGURATION_CACHE_REASON;
        }

        @SuppressWarnings("unchecked")
        val taskType = (Class<Task>) task.getClass();
        val notCompatibleWithConfigurationCacheMethod = findMethod(
            taskType,
            "notCompatibleWithConfigurationCache",
            String.class
        );
        if (notCompatibleWithConfigurationCacheMethod != null) {
            notCompatibleWithConfigurationCacheMethod.invoke(task, reason);
        }
    }


    public static boolean isInTaskGraph(Task task) {
        return task.getProject()
            .getGradle()
            .getTaskGraph()
            .hasTask(task);
    }

    public static boolean isRequested(Task task) {
        val project = task.getProject();

        val startParameter = project.getGradle().getStartParameter();
        val requestedProjectPath = Optional.ofNullable(startParameter.getProjectDir())
            .map(File::toPath)
            .map(PathUtils::normalizePath)
            .orElse(null);
        if (requestedProjectPath != null) {
            val projectPath = normalizePath(project.getProjectDir().toPath());
            if (!projectPath.startsWith(requestedProjectPath)) {
                return false;
            }
        }

        return startParameter.getTaskNames().stream()
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
    @ReliesOnInternalGradleApi
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

}
