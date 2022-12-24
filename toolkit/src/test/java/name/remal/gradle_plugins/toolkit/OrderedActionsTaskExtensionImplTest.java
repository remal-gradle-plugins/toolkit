package name.remal.gradle_plugins.toolkit;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Test;

class OrderedActionsTaskExtensionImplTest {

    @Test
    void freezeAction() {
        val isExecuted = new AtomicBoolean(false);

        @Data
        class MutableAction implements OrderedAction<Object> {
            private String id;
            private int stage;
            private Collection<String> shouldBeExecutedAfter;
            private Collection<String> shouldBeExecutedBefore;

            @Override
            public void execute(Object unused) {
                isExecuted.set(true);
            }
        }

        val mutableAction = new MutableAction();
        mutableAction.setId("1");
        mutableAction.setStage(1);
        mutableAction.setShouldBeExecutedAfter(List.of("0"));
        mutableAction.setShouldBeExecutedBefore(List.of("2"));

        val frozenAction = OrderedActionsTaskExtensionImpl.freezeAction(mutableAction);

        mutableAction.setId("10");
        mutableAction.setStage(10);
        mutableAction.setShouldBeExecutedAfter(List.of("00"));
        mutableAction.setShouldBeExecutedBefore(List.of("20"));

        assertThat(frozenAction.getId()).isEqualTo("1");
        assertThat(frozenAction.getStage()).isEqualTo(1);
        assertThat(frozenAction.getShouldBeExecutedAfter()).containsExactlyInAnyOrder("0");
        assertThat(frozenAction.getShouldBeExecutedBefore()).containsExactlyInAnyOrder("2");

        assertThat(isExecuted.get()).isFalse();
        frozenAction.execute(new Object[0]);
        assertThat(isExecuted.get()).isTrue();
    }

    @Test
    void getDependenciesMap() {
        @Value
        class OrderedActionImpl implements OrderedAction<Object> {
            String id;
            Collection<String> shouldBeExecutedAfter;
            Collection<String> shouldBeExecutedBefore;

            @Override
            public void execute(Object unused) {
                //do nothing
            }
        }

        val actions = List.of(
            new OrderedActionImpl("4", List.of(), List.of()),
            new OrderedActionImpl("3", List.of("2", "1"), List.of("4")),
            new OrderedActionImpl("1", List.of(), List.of()),
            new OrderedActionImpl("2", List.of("1"), List.of())
        );

        val dependenciesMap = OrderedActionsTaskExtensionImpl.getDependenciesMap(actions);
        assertThat(dependenciesMap).containsExactlyInAnyOrderEntriesOf(Map.of(
            "4", Set.of("3"),
            "3", Set.of("2", "1"),
            "2", Set.of("1")
        ));
    }

    @Test
    void sortStagedActions() {
        @Value
        class OrderedActionImpl implements OrderedAction<Object> {
            String id;
            Collection<String> shouldBeExecutedAfter;
            Collection<String> shouldBeExecutedBefore;

            @Override
            public void execute(Object unused) {
                //do nothing
            }
        }

        val actions = List.of(
            new OrderedActionImpl("0", List.of(), List.of()),
            new OrderedActionImpl("5", List.of("4"), List.of()),
            new OrderedActionImpl("4", List.of(), List.of()),
            new OrderedActionImpl("1", List.of(), List.of("4")),
            new OrderedActionImpl("3", List.of("2", "1"), List.of("4")),
            new OrderedActionImpl("2", List.of("1"), List.of()),
            new OrderedActionImpl("6", List.of(), List.of())
        );

        val sortedActions = OrderedActionsTaskExtensionImpl.sortStagedActions(actions);
        val sortedIds = sortedActions.stream()
            .map(OrderedAction::getId)
            .collect(toUnmodifiableList());
        assertThat(sortedIds).containsExactly("0", "1", "2", "3", "4", "5", "6");
    }

}
