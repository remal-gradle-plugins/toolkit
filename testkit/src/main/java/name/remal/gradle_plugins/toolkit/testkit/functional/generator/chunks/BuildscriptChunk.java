package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import name.remal.gradle_plugins.generate_sources.generators.TextContentChunk;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;

public interface BuildscriptChunk<Block extends JavaLikeContent<Block>>
    extends TextContentChunk, WithBuildscript<Block> {
}
