package name.remal.gradle_plugins.toolkit;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
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
        mutableAction.setShouldBeExecutedAfter(ImmutableList.of("0"));
        mutableAction.setShouldBeExecutedBefore(ImmutableList.of("2"));

        val frozenAction = OrderedActionsTaskExtensionImpl.freezeAction(mutableAction);

        mutableAction.setId("10");
        mutableAction.setStage(10);
        mutableAction.setShouldBeExecutedAfter(ImmutableList.of("00"));
        mutableAction.setShouldBeExecutedBefore(ImmutableList.of("20"));

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

        val actions = ImmutableList.of(
            new OrderedActionImpl("4", ImmutableList.of(), ImmutableList.of()),
            new OrderedActionImpl("3", ImmutableList.of("2", "1"), ImmutableList.of("4")),
            new OrderedActionImpl("1", ImmutableList.of(), ImmutableList.of()),
            new OrderedActionImpl("2", ImmutableList.of("1"), ImmutableList.of())
        );

        val dependenciesMap = OrderedActionsTaskExtensionImpl.getDependenciesMap(actions);
        assertThat(dependenciesMap).containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
            "4", ImmutableSet.of("3"),
            "3", ImmutableSet.of("2", "1"),
            "2", ImmutableSet.of("1")
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

        val actions = ImmutableList.of(
            new OrderedActionImpl("0", ImmutableList.of(), ImmutableList.of()),
            new OrderedActionImpl("5", ImmutableList.of("4"), ImmutableList.of()),
            new OrderedActionImpl("4", ImmutableList.of(), ImmutableList.of()),
            new OrderedActionImpl("1", ImmutableList.of(), ImmutableList.of("4")),
            new OrderedActionImpl("3", ImmutableList.of("2", "1"), ImmutableList.of("4")),
            new OrderedActionImpl("2", ImmutableList.of("1"), ImmutableList.of()),
            new OrderedActionImpl("6", ImmutableList.of(), ImmutableList.of())
        );

        val sortedActions = OrderedActionsTaskExtensionImpl.sortStagedActions(actions);
        val sortedIds = sortedActions.stream()
            .map(OrderedAction::getId)
            .collect(toList());
        assertThat(sortedIds).containsExactly("0", "1", "2", "3", "4", "5", "6");
    }

}
