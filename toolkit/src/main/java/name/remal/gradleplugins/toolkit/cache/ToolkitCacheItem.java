package name.remal.gradleplugins.toolkit.cache;

import java.nio.file.attribute.FileTime;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString(of = "lastModified")
public class ToolkitCacheItem<T> {
    @Nullable
    private final FileTime lastModified;
    private final T value;
}
