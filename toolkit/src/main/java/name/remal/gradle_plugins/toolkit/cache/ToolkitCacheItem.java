package name.remal.gradle_plugins.toolkit.cache;

import java.nio.file.attribute.FileTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
@Getter
@ToString(of = "lastModified")
public class ToolkitCacheItem<T> {
    @Nullable
    private final FileTime lastModified;
    private final T value;
}
