package name.remal.gradle_plugins.toolkit.cache.files;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@ToString
@SuppressWarnings("NullableProblems")
class ToolkitFilesCacheParams<KEY> {

    @NonNull
    protected final Path baseDir;

    @NonNull
    protected final ToolkitFilesCacheKeyEncoder<KEY> keyEncoder;

    @Singular
    protected final List<ToolkitFilesCacheField<?>> fields;

    @Default
    protected final Duration lastAccessRetention = Duration.ofDays(7);

    @Default
    protected final Clock clock = Clock.systemDefaultZone();

}
