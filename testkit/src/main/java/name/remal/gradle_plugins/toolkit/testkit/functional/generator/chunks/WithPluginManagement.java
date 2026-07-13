package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import org.gradle.api.Action;

public interface WithPluginManagement<Block extends JavaLikeContent<Block>> {

    Block getPluginManagement();

    default void forPluginManagement(Action<? super Block> action) {
        action.execute(getPluginManagement());
    }

}
