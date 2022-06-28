package name.remal.gradleplugins.toolkit.testkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;

@NoArgsConstructor(access = PRIVATE)
public abstract class TaskActionsExecutor {

    public static boolean executeOnlyIfSpecs(Task task) {
        val taskInternal = (TaskInternal) task;
        return taskInternal.getOnlyIf().isSatisfiedBy(taskInternal);
    }

    public static void executeActions(Task task) {
        val actions = task.getActions();
        for (val action : actions) {
            action.execute(task);
        }
    }

}
