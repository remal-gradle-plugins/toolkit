package name.remal.gradleplugins.toolkit.testkit;

import static lombok.AccessLevel.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class TaskActionsExecutorTest {

    private final Project project;

    @Test
    void executeOnlyIfSpecs() {
        val task = project.getTasks().create("test");

        val executedOnlyIfSpecs = new AtomicInteger(0);
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

        val result = TaskActionsExecutor.executeOnlyIfSpecs(task);
        assertThat(result).isFalse();
        assertThat(executedOnlyIfSpecs.get()).isEqualTo(2);
    }

    @Test
    void executeActions() {
        val task = project.getTasks().create("test", TestTask.class);

        val isDoFirstExecuted = new AtomicBoolean(false);
        task.doFirst(__ -> isDoFirstExecuted.set(true));

        val isDoLastExecuted = new AtomicBoolean(false);
        task.doLast(__ -> isDoLastExecuted.set(true));

        assertThat(task.isExecuted.get()).isFalse();
        assertThat(isDoFirstExecuted.get()).isFalse();
        assertThat(isDoLastExecuted.get()).isFalse();

        TaskActionsExecutor.executeActions(task);

        assertThat(task.isExecuted.get()).isTrue();
        assertThat(isDoFirstExecuted.get()).isTrue();
        assertThat(isDoLastExecuted.get()).isTrue();
    }

    @NoArgsConstructor(access = PUBLIC)
    static class TestTask extends DefaultTask {
        public final AtomicBoolean isExecuted = new AtomicBoolean(false);

        @TaskAction
        public void execute() {
            isExecuted.set(true);
        }
    }

}
