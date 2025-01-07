package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import org.gradle.api.Action;

public interface WithBuildscript<Block extends JavaLikeContent<Block>> {

    Block getBuildscript();

    default void forBuildscript(Action<Block> action) {
        action.execute(getBuildscript());
    }

}
