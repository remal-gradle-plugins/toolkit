package name.remal.gradleplugins.toolkit;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.toUnique;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.PluginManager;

@NoArgsConstructor(access = PRIVATE)
public abstract class PluginManagerUtils {

    public static void withAnyOfPlugins(
        PluginManager pluginManager,
        Iterable<String> pluginIds,
        Action<AppliedPlugin> action
    ) {
        val isExecuted = new AtomicBoolean(false);
        toUnique(pluginIds).forEach(pluginId ->
            pluginManager.withPlugin(pluginId, appliedPlugin -> {
                if (isExecuted.compareAndSet(false, true)) {
                    action.execute(appliedPlugin);
                }
            })
        );
    }

    public static void withAnyOfPlugins(
        PluginManager pluginManager,
        String pluginId1,
        Action<AppliedPlugin> action
    ) {
        withAnyOfPlugins(
            pluginManager,
            singletonList(pluginId1),
            action
        );
    }

    public static void withAnyOfPlugins(
        PluginManager pluginManager,
        String pluginId1,
        String pluginId2,
        Action<AppliedPlugin> action
    ) {
        withAnyOfPlugins(
            pluginManager,
            asList(pluginId1, pluginId2),
            action
        );
    }

    public static void withAnyOfPlugins(
        PluginManager pluginManager,
        String pluginId1,
        String pluginId2,
        String pluginId3,
        Action<AppliedPlugin> action
    ) {
        withAnyOfPlugins(
            pluginManager,
            asList(pluginId1, pluginId2, pluginId3),
            action
        );
    }

    public static void withAnyOfPlugins(
        PluginManager pluginManager,
        String pluginId1,
        String pluginId2,
        String pluginId3,
        String pluginId4,
        Action<AppliedPlugin> action
    ) {
        withAnyOfPlugins(
            pluginManager,
            asList(pluginId1, pluginId2, pluginId3, pluginId4),
            action
        );
    }

    public static void withAnyOfPlugins(
        PluginManager pluginManager,
        String pluginId1,
        String pluginId2,
        String pluginId3,
        String pluginId4,
        String pluginId5,
        Action<AppliedPlugin> action
    ) {
        withAnyOfPlugins(
            pluginManager,
            asList(pluginId1, pluginId2, pluginId3, pluginId4, pluginId5),
            action
        );
    }


    public static void withAllPlugins(
        PluginManager pluginManager,
        Iterable<String> pluginIds,
        Action<AppliedPlugin> action
    ) {
        val pluginIdsToApply = toUnique(pluginIds);
        new ArrayList<>(pluginIdsToApply).forEach(pluginId ->
            pluginManager.withPlugin(pluginId, appliedPlugin -> {
                synchronized (pluginIdsToApply) {
                    pluginIdsToApply.remove(pluginId);
                    if (pluginIdsToApply.isEmpty()) {
                        action.execute(appliedPlugin);
                    }
                }
            })
        );
    }

    public static void withAllPlugins(
        PluginManager pluginManager,
        String pluginId1,
        Action<AppliedPlugin> action
    ) {
        withAllPlugins(
            pluginManager,
            singletonList(pluginId1),
            action
        );
    }

    public static void withAllPlugins(
        PluginManager pluginManager,
        String pluginId1,
        String pluginId2,
        Action<AppliedPlugin> action
    ) {
        withAllPlugins(
            pluginManager,
            asList(pluginId1, pluginId2),
            action
        );
    }

    public static void withAllPlugins(
        PluginManager pluginManager,
        String pluginId1,
        String pluginId2,
        String pluginId3,
        Action<AppliedPlugin> action
    ) {
        withAllPlugins(
            pluginManager,
            asList(pluginId1, pluginId2, pluginId3),
            action
        );
    }

    public static void withAllPlugins(
        PluginManager pluginManager,
        String pluginId1,
        String pluginId2,
        String pluginId3,
        String pluginId4,
        Action<AppliedPlugin> action
    ) {
        withAllPlugins(
            pluginManager,
            asList(pluginId1, pluginId2, pluginId3, pluginId4),
            action
        );
    }

    public static void withAllPlugins(
        PluginManager pluginManager,
        String pluginId1,
        String pluginId2,
        String pluginId3,
        String pluginId4,
        String pluginId5,
        Action<AppliedPlugin> action
    ) {
        withAllPlugins(
            pluginManager,
            asList(pluginId1, pluginId2, pluginId3, pluginId4, pluginId5),
            action
        );
    }

}
