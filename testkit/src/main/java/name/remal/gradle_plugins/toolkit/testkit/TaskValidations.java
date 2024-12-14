package name.remal.gradle_plugins.toolkit.testkit;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionGreaterThanOrEqualTo;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeMethod;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeStaticMethod;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.StringUtils;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.problems.Problems;
import org.gradle.api.problems.internal.InternalProblems;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.execution.plan.LocalTaskNode;
import org.gradle.execution.plan.TaskNode;
import org.gradle.execution.plan.TaskNodeFactory;
import org.gradle.internal.reflect.validation.TypeValidationProblemRenderer;

@NoArgsConstructor(access = PRIVATE)
@ReliesOnInternalGradleApi
@CustomLog
public abstract class TaskValidations {

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

    public static void assertNoTaskPropertiesProblems(Task task) {
        if (isCurrentGradleVersionGreaterThanOrEqualTo("7.0")) {
            assertNoTaskPropertiesProblemsImpl(task, false);
        }
    }

    @VisibleForTesting
    @SneakyThrows
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    static void assertNoTaskPropertiesProblemsImpl(Task task, boolean rethrowExceptions) {
        val taskType = unwrapGeneratedSubclass(task.getClass());

        final Collection<?> problems;
        try {
            if (isCurrentGradleVersionGreaterThanOrEqualTo("8.12")) {
                val services = ((ProjectInternal) task.getProject()).getServices();
                val problemsService = (InternalProblems) services.get(Problems.class);
                val problemsProgressEventEmitterHolderClass = Class.forName(
                    "org.gradle.api.problems.internal.ProblemsProgressEventEmitterHolder"
                );
                invokeStaticMethod(
                    problemsProgressEventEmitterHolderClass,
                    "init",
                    InternalProblems.class, problemsService
                );
            }

            val taskNode = getLocalTaskNode(task);
            taskNode.resolveMutations();

            val validationContext = taskNode.getValidationContext();
            val typeValidationContext = validationContext.forType(taskType, false);
            taskNode.getTaskProperties().validateType(typeValidationContext);

            problems = invokeMethod(validationContext, Collection.class, "getProblems");

        } catch (Throwable e) {
            if (rethrowExceptions
                || e instanceof TaskExecutionException
            ) {
                throw e;
            }
            logger.error(e.toString(), e);
            return;
        }

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

    @SuppressWarnings("unchecked")
    private static LocalTaskNode getLocalTaskNode(Task task) {
        val taskNodeFactory = ((ProjectInternal) task.getProject()).getServices().get(TaskNodeFactory.class);

        val getOrCreateNodeWithOrdinal = findMethod(
            (Class<TaskNodeFactory>) taskNodeFactory.getClass(),
            TaskNode.class,
            "getOrCreateNode",
            Task.class,
            int.class
        );
        if (getOrCreateNodeWithOrdinal != null) {
            return (LocalTaskNode) getOrCreateNodeWithOrdinal.invoke(taskNodeFactory, task, -1);
        }

        return (LocalTaskNode) taskNodeFactory.getOrCreateNode(task);
    }

    @SneakyThrows
    private static String renderProblem(Object problem) {
        return invokeStaticMethod(
            TypeValidationProblemRenderer.class,
            String.class, "renderMinimalInformationAbout",
            problem.getClass(), problem
        );
    }

}
