package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.testkit.TaskValidations.executeActions;
import static name.remal.gradle_plugins.toolkit.testkit.TaskValidations.executeOnlyIfSpecs;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.intellij.lang.annotations.Pattern;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class OrderedActionUtilsTest {

    final Project project;

    @Test
    void doFirstLastOrdered() {
        var task = project.getTasks().register("test").get();

        Set<String> executedDoFirstActions = new LinkedHashSet<>();
        OrderedActionUtils.doFirstOrdered(task, new OrderedAction<Task>() {
            @Override
            @Pattern("[\\w.-]+")
            public String getId() {
                return "2";
            }

            @Override
            public void execute(Task task) {
                executedDoFirstActions.add(getId());
            }
        });
        OrderedActionUtils.doFirstOrdered(task, new OrderedAction<Task>() {
            @Override
            @Pattern("[\\w.-]+")
            public String getId() {
                return "1";
            }

            @Override
            public Collection<String> getShouldBeExecutedBefore() {
                return List.of("2");
            }

            @Override
            public void execute(Task task) {
                executedDoFirstActions.add(getId());
            }
        });
        OrderedActionUtils.doFirstOrdered(task, new OrderedAction<Task>() {
            @Override
            @Pattern("[\\w.-]+")
            public String getId() {
                return "first";
            }

            @Override
            public void execute(Task task) {
                executedDoFirstActions.add(getId());
            }
        });

        Set<String> executedDoLastActions = new LinkedHashSet<>();
        OrderedActionUtils.doLastOrdered(task, new OrderedAction<Task>() {
            @Override
            @Pattern("[\\w.-]+")
            public String getId() {
                return "2";
            }

            @Override
            public Collection<String> getShouldBeExecutedAfter() {
                return List.of("1");
            }

            @Override
            public void execute(Task task) {
                executedDoLastActions.add(getId());
            }
        });
        OrderedActionUtils.doLastOrdered(task, new OrderedAction<Task>() {
            @Override
            @Pattern("[\\w.-]+")
            public String getId() {
                return "last";
            }

            @Override
            public void execute(Task task) {
                executedDoLastActions.add(getId());
            }
        });
        OrderedActionUtils.doLastOrdered(task, new OrderedAction<Task>() {
            @Override
            @Pattern("[\\w.-]+")
            public String getId() {
                return "1";
            }

            @Override
            public void execute(Task task) {
                executedDoLastActions.add(getId());
            }
        });

        assertThat(executedDoFirstActions).isEmpty();
        assertThat(executedDoLastActions).isEmpty();

        executeOnlyIfSpecs(task);
        executeActions(task);

        assertThat(executedDoFirstActions).containsExactly("1", "2", "first");
        assertThat(executedDoLastActions).containsExactly("1", "2", "last");
    }

}
