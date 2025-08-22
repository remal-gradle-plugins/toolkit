package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import java.time.Duration;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public class DefaultTaskTimeoutChunkDefault<Block extends JavaLikeContent<Block>>
    implements DefaultTaskTimeoutChunk {

    private static final Duration DEFAULT_TASK_TIMEOUT = Duration.ofMinutes(1);


    private final Supplier<Block> blockFactory;

    @Setter
    @Nullable
    private Duration defaultTaskTimeout = DEFAULT_TASK_TIMEOUT;

    @Override
    public String toString() {
        if (defaultTaskTimeout == null) {
            return "";
        }

        var content = blockFactory.get();
        content.block("tasks.configureEach", task -> {
            task.line("timeout = Duration.parse(\"%s\")", content.escapeString(defaultTaskTimeout.toString()));
        });
        return content.toString();
    }

}
