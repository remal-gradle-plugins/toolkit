package name.remal.gradle_plugins.toolkit.testkit.functional.generator;

import java.util.Set;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeFileContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.BuildscriptChunk;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.PluginsChunk;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.WithBuildscript;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.WithPlugins;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

public interface GradleFileContent<Block extends JavaLikeContent<Block>>
    extends JavaLikeFileContent<Block>, WithBuildscript<Block>, WithPlugins {

    @Override
    @SuppressWarnings("unchecked")
    default Block getBuildscript() {
        return (Block) getChunk(BuildscriptChunk.class).getBuildscript();
    }

    @Override
    default void applyPlugin(String pluginId, @Nullable Object version) {
        getChunk(PluginsChunk.class).applyPlugin(pluginId, version);
    }

    @Override
    default void applyPluginAtTheBeginning(String pluginId, @Nullable Object version) {
        getChunk(PluginsChunk.class).applyPluginAtTheBeginning(pluginId, version);
    }

    @Override
    @Unmodifiable
    default Set<String> getAppliedPlugins() {
        return getChunk(PluginsChunk.class).getAppliedPlugins();
    }

}
