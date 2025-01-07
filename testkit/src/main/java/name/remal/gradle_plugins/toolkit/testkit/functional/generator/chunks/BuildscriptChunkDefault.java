package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import java.util.function.Supplier;
import lombok.Getter;
import lombok.val;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;

public class BuildscriptChunkDefault<Block extends JavaLikeContent<Block>>
    implements BuildscriptChunk<Block> {

    @Getter
    private final Block buildscript;

    private final Supplier<Block> blockFactory;

    public BuildscriptChunkDefault(Supplier<Block> blockFactory) {
        this.buildscript = blockFactory.get();
        this.blockFactory = blockFactory;
    }

    @Override
    public String toString() {
        if (!buildscript.hasChunks()) {
            return "";
        }

        val wrapper = blockFactory.get();
        wrapper.block("buildscript", inner ->
            inner.line(buildscript)
        );
        return wrapper.toString();
    }

}
