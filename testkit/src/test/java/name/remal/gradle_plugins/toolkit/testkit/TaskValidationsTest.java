package name.remal.gradle_plugins.toolkit.testkit;

import static lombok.AccessLevel.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class TaskValidationsTest {

    private final Project project;

    @Test
    void executeOnlyIfSpecs() {
        var task = project.getTasks().register("test").get();

        var executedOnlyIfSpecs = new AtomicInteger(0);
        task.onlyIf(__ -> {
            executedOnlyIfSpecs.incrementAndGet();
            return true;
        });
        task.onlyIf(__ -> {
            executedOnlyIfSpecs.incrementAndGet();
            return false;
        });
        task.onlyIf(__ -> {
            executedOnlyIfSpecs.incrementAndGet();
            return true;
        });

        var result = TaskValidations.executeOnlyIfSpecs(task);
        assertThat(result).isFalse();
        assertThat(executedOnlyIfSpecs.get()).isEqualTo(2);
    }

    @Test
    void executeActions() {
        var task = project.getTasks().register("test", TestTaskForExecuteActions.class).get();

        var isDoFirstExecuted = new AtomicBoolean(false);
        task.doFirst(__ -> isDoFirstExecuted.set(true));

        var isDoLastExecuted = new AtomicBoolean(false);
        task.doLast(__ -> isDoLastExecuted.set(true));

        assertThat(task.isExecuted.get()).isFalse();
        assertThat(isDoFirstExecuted.get()).isFalse();
        assertThat(isDoLastExecuted.get()).isFalse();

        TaskValidations.executeActions(task);

        assertThat(task.isExecuted.get()).isTrue();
        assertThat(isDoFirstExecuted.get()).isTrue();
        assertThat(isDoLastExecuted.get()).isTrue();
    }

    @NoArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
    static class TestTaskForExecuteActions extends DefaultTask {

        public final AtomicBoolean isExecuted = new AtomicBoolean(false);

        @TaskAction
        public void execute() {
            isExecuted.set(true);
        }

    }


    @Test
    void markTaskAsSkipped() {
        var task = project.getTasks().register("task").get();
        assertDoesNotThrow(() -> TaskValidations.markTaskAsSkipped(task));
    }

    @Test
    void markTaskAsExecuted() {
        var task = project.getTasks().register("task").get();
        assertDoesNotThrow(() -> TaskValidations.markTaskAsExecuted(task));
    }

    @Test
    void markTaskDependenciesAsSkipped() {
        var task = project.getTasks().register("task").get();
        assertDoesNotThrow(() -> TaskValidations.markTaskDependenciesAsSkipped(task));
    }

    @Test
    void markTaskDependenciesAsExecuted() {
        var task = project.getTasks().register("task").get();
        assertDoesNotThrow(() -> TaskValidations.markTaskDependenciesAsExecuted(task));
    }


    @Test
    @MinTestableGradleVersion("7.0")
    void assertNoTaskPropertiesProblems_without_problems() {
        var task = project.getTasks().register("task", TestTaskWithoutPropertyProblems.class).get();
        assertDoesNotThrow(() -> TaskValidations.assertNoTaskPropertiesProblemsImpl(task, true));
    }

    @NoArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
    static class TestTaskWithoutPropertyProblems extends DefaultTask {

        @Getter(onMethod_ = {@Nested})
        private final Map<String, String> map = new LinkedHashMap<>();

        @Getter(onMethod_ = {@Input})
        private int number;

    }

    @Test
    @MinTestableGradleVersion("7.0")
    void assertNoTaskPropertiesProblems_with_problems() {
        var task = project.getTasks().register("task", TestTaskWithPropertyProblems.class).get();
        assertThrows(AssertionError.class, () -> TaskValidations.assertNoTaskPropertiesProblemsImpl(task, true));
    }

    @NoArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
    static class TestTaskWithPropertyProblems extends DefaultTask {

        @Getter(onMethod_ = {@Nested})
        @SuppressWarnings("rawtypes")
        private final Map map = new LinkedHashMap();

        @Getter(onMethod_ = {@Input, @org.gradle.api.tasks.Optional})
        private int number;

    }

}
