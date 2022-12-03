package name.remal.gradleplugins.toolkit.testkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.reflection.MethodsInvoker.invokeMethod;

import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradleplugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.specs.Spec;

@NoArgsConstructor(access = PRIVATE)
public abstract class TaskActionsExecutor {

    @ReliesOnInternalGradleApi
    @SuppressWarnings("unchecked")
    public static boolean executeOnlyIfSpecs(Task task) {
        val taskInternal = (TaskInternal) task;
        val spec = invokeMethod(taskInternal, Spec.class, "getOnlyIf");
        return spec.isSatisfiedBy(taskInternal);
    }

    public static void executeActions(Task task) {
        val actions = task.getActions();
        for (val action : actions) {
            action.execute(task);
        }
    }

}
