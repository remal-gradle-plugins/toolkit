package name.remal.gradle_plugins.toolkit.testkit.functional.generator;

import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.PluginManagementChunk;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.WithPluginManagement;

public interface GradleSettingsFileContent<Block extends JavaLikeContent<Block>>
    extends GradleFileContent<Block>, WithPluginManagement<Block> {

    void setWithFoojayToolchainsResolver(boolean withFoojayToolchainsResolver);

    @Override
    @SuppressWarnings("unchecked")
    default Block getPluginManagement() {
        return (Block) getChunk(PluginManagementChunk.class).getPluginManagement();
    }

}
