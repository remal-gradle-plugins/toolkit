package name.remal.gradle_plugins.toolkit.testkit.functional.generator;

import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.DefaultTaskChunk;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.WithDefaultTask;

public interface GradleBuildFileContent<Block extends JavaLikeContent<Block>>
    extends GradleChildBuildFileContent<Block>, WithDefaultTask {

    @Override
    default void setDefaultTask(String defaultTask) {
        getChunk(DefaultTaskChunk.class).setDefaultTask(defaultTask);
    }

}
