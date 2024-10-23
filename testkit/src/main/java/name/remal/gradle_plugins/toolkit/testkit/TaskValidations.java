package name.remal.gradle_plugins.toolkit.testkit;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeMethod;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeStaticMethod;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import java.util.Collection;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.StringUtils;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.specs.Spec;
import org.gradle.execution.plan.LocalTaskNode;
import org.gradle.execution.plan.TaskNodeFactory;
import org.gradle.internal.reflect.validation.TypeValidationProblemRenderer;

@NoArgsConstructor(access = PRIVATE)
public abstract class TaskValidations {

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

    @ReliesOnInternalGradleApi
    public static void assertNoTaskPropertiesProblems(Task task) {
        val taskNodeFactory = ((ProjectInternal) task.getProject()).getServices().get(TaskNodeFactory.class);
        val taskNode = (LocalTaskNode) taskNodeFactory.getOrCreateNode(task);
        taskNode.resolveMutations();

        val taskType = unwrapGeneratedSubclass(task.getClass());
        val validationContext = taskNode.getValidationContext();
        val typeValidationContext = validationContext.forType(taskType, false);
        taskNode.getTaskProperties().validateType(typeValidationContext);

        Collection<?> problems = invokeMethod(validationContext, Collection.class, "getProblems");
        if (isNotEmpty(problems)) {
            throw new AssertionError(format(
                "%d problems found with task %s (%s):%s",
                problems.size(),
                task.getPath(),
                taskType,
                problems.stream()
                    .map(TaskValidations::renderProblem)
                    .map(StringUtils::normalizeString)
                    .map(string -> string.replace("\n", "\n  "))
                    .collect(joining("\n- ", "\n- ", ""))
            ));
        }
    }

    @SneakyThrows
    @ReliesOnInternalGradleApi
    private static String renderProblem(Object problem) {
        return invokeStaticMethod(
            TypeValidationProblemRenderer.class,
            String.class, "renderMinimalInformationAbout",
            problem.getClass(), problem
        );
    }

}
