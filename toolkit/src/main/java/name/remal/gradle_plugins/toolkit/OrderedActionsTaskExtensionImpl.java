package name.remal.gradle_plugins.toolkit;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.lang.reflect.Proxy.newProxyInstance;
import static lombok.AccessLevel.PUBLIC;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.equalsTo;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;
import static name.remal.gradle_plugins.toolkit.ProxyUtils.toDynamicInterface;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;

@NoArgsConstructor(access = PUBLIC)
class OrderedActionsTaskExtensionImpl implements OrderedActionsTaskExtension {

    private final List<OrderedAction<?>> actions = new ArrayList<>();

    @Override
    @SneakyThrows
    @SuppressWarnings("rawtypes")
    public void add(Object untypedAction) {
        OrderedAction action = toDynamicInterface(untypedAction, OrderedAction.class);
        actions.add(action);
    }


    @Override
    @Unmodifiable
    public List<OrderedAction<?>> getActions() {
        SortedMap<Integer, List<OrderedAction<?>>> allStagedActions = new TreeMap<>();
        Set<String> ids = new LinkedHashSet<>();
        for (val action : actions) {
            val frozenAction = freezeAction(action);

            val id = frozenAction.getId();
            if (!ids.add(id)) {
                throw new IllegalStateException(format(
                    "Multiple instances of %s with ID='%s'",
                    OrderedAction.class.getSimpleName(),
                    id
                ));
            }

            val stage = frozenAction.getStage();
            allStagedActions.computeIfAbsent(stage, __ -> new ArrayList<>())
                .add(frozenAction);
        }

        return allStagedActions.values().stream()
            .map(OrderedActionsTaskExtensionImpl::sortStagedActions)
            .flatMap(Collection::stream)
            .collect(toImmutableList());
    }

    @VisibleForTesting
    @SuppressWarnings("unchecked")
    static <T> OrderedAction<T> freezeAction(OrderedAction<T> action) {
        val id = action.getId();
        val stage = action.getStage();
        val shouldBeExecutedAfter = ImmutableList.copyOf(action.getShouldBeExecutedAfter());
        val shouldBeExecutedBefore = ImmutableList.copyOf(action.getShouldBeExecutedBefore());
        return (OrderedAction<T>) newProxyInstance(
            OrderedAction.class.getClassLoader(),
            new Class<?>[]{OrderedAction.class},
            new ProxyInvocationHandler()
                .add(
                    method -> CharSequence.class.isAssignableFrom(method.getReturnType())
                        && method.getParameterCount() == 0
                        && method.getName().equals("getId"),
                    (proxy, method, args) -> id
                )
                .add(
                    method -> int.class == method.getReturnType()
                        && method.getParameterCount() == 0
                        && method.getName().equals("getStage"),
                    (proxy, method, args) -> stage
                )
                .add(
                    method -> Iterable.class.isAssignableFrom(method.getReturnType())
                        && method.getParameterCount() == 0
                        && method.getName().equals("getShouldBeExecutedAfter"),
                    (proxy, method, args) -> shouldBeExecutedAfter
                )
                .add(
                    method -> Iterable.class.isAssignableFrom(method.getReturnType())
                        && method.getParameterCount() == 0
                        && method.getName().equals("getShouldBeExecutedBefore"),
                    (proxy, method, args) -> shouldBeExecutedBefore
                )
                .add(__ -> true, (proxy, method, args) -> method.invoke(action, args))
        );
    }

    @VisibleForTesting
    @SuppressWarnings("unchecked")
    static List<OrderedAction<?>> sortStagedActions(List<? extends OrderedAction<?>> stagedActions) {
        if (stagedActions.isEmpty() || stagedActions.size() == 1) {
            return (List<OrderedAction<?>>) stagedActions;
        }

        val dependenciesMap = getDependenciesMap(stagedActions);
        if (dependenciesMap.isEmpty()) {
            return (List<OrderedAction<?>>) stagedActions;
        }

        List<OrderedAction<?>> result = new ArrayList<>(stagedActions.size());
        insertSortedStagedActions(result, new ArrayList<>(stagedActions), dependenciesMap, null);
        return result;
    }

    @SuppressWarnings("java:S3776")
    private static void insertSortedStagedActions(
        List<OrderedAction<?>> result,
        List<OrderedAction<?>> stagedActions,
        Map<String, Set<String>> dependenciesMap,
        @Nullable Set<String> currentDependencies
    ) {
        if (currentDependencies == null) {
            while (!stagedActions.isEmpty()) {
                val action = stagedActions.remove(0);
                val actionDependencies = dependenciesMap.get(action.getId());
                if (isNotEmpty(actionDependencies) && !stagedActions.isEmpty()) {
                    insertSortedStagedActions(result, stagedActions, dependenciesMap, actionDependencies);
                }
                result.add(action);
            }

        } else {
            for (val dependencyId : currentDependencies) {
                while (!stagedActions.isEmpty()) {
                    OrderedAction<?> action = null;
                    for (int i = 0; i < stagedActions.size(); ++i) {
                        val curAction = stagedActions.get(i);
                        if (Objects.equals(curAction.getId(), dependencyId)) {
                            action = curAction;
                            stagedActions.remove(i);
                            break;
                        }
                    }

                    if (action == null) {
                        break;
                    }

                    val actionDependencies = dependenciesMap.get(action.getId());
                    if (isNotEmpty(actionDependencies) && !stagedActions.isEmpty()) {
                        insertSortedStagedActions(result, stagedActions, dependenciesMap, actionDependencies);
                    }
                    result.add(action);
                }
            }
        }
    }

    @VisibleForTesting
    static Map<String, Set<String>> getDependenciesMap(List<? extends OrderedAction<?>> stagedActions) {
        Map<String, Set<String>> dependenciesMap = new LinkedHashMap<>();
        stagedActions.forEach(action -> {
            val actionId = action.getId();
            action.getShouldBeExecutedAfter().stream()
                .filter(Objects::nonNull)
                .filter(not(equalsTo(actionId)))
                .forEach(dependency -> {
                    dependenciesMap.computeIfAbsent(actionId, __ -> new LinkedHashSet<>())
                        .add(dependency);
                });
            action.getShouldBeExecutedBefore().stream()
                .filter(Objects::nonNull)
                .filter(not(equalsTo(actionId)))
                .forEach(dependency -> {
                    dependenciesMap.computeIfAbsent(dependency, __ -> new LinkedHashSet<>())
                        .add(actionId);
                });
        });
        return dependenciesMap;
    }

}
