package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface WithPlugins {

    void applyPlugin(String pluginId, @Nullable Object version);

    default void applyPlugin(
        String pluginId,
        @Nullable Supplier<@org.jetbrains.annotations.Nullable Object> versionSupplier
    ) {
        applyPlugin(pluginId, (Object) versionSupplier);
    }

    default void applyPlugin(String pluginId) {
        applyPlugin(pluginId, "");
    }


    void applyPluginAtTheBeginning(String pluginId, @Nullable Object version);

    default void applyPluginAtTheBeginning(
        String pluginId,
        @Nullable Supplier<@org.jetbrains.annotations.Nullable Object> versionSupplier
    ) {
        applyPluginAtTheBeginning(pluginId, (Object) versionSupplier);
    }

    default void applyPluginAtTheBeginning(String pluginId) {
        applyPluginAtTheBeginning(pluginId, "");
    }


    @Unmodifiable
    Set<String> getAppliedPlugins();

    default boolean isPluginApplied(String pluginId) {
        var appliedPlugins = getAppliedPlugins();
        return appliedPlugins.contains(pluginId)
            || appliedPlugins.contains("org.gradle." + pluginId);
    }

}
