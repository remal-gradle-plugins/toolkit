package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.testkit.TaskValidations.executeActions;
import static name.remal.gradle_plugins.toolkit.testkit.TaskValidations.executeOnlyIfSpecs;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.intellij.lang.annotations.Pattern;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class OrderedActionUtilsTest {

    private final Project project;

    @Test
    void doFirstLastOrdered() {
        val task = project.getTasks().create("test");

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
                return ImmutableList.of("2");
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
                return ImmutableList.of("1");
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
