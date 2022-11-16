package name.remal.gradleplugins.toolkit.cache;

import static lombok.AccessLevel.PRIVATE;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class ToolkitCaches {

    public static <Value> ToolkitCache<Path, Value> newPathToolkitCache(ToolkitCacheLoader<Path, Value> loader) {
        return new ToolkitCacheBuilder<Path, Value>()
            .withLoader(loader)
            .build();
    }

    public static <Value> ToolkitCache<Path, Value> newWeakPathToolkitCache(ToolkitCacheLoader<Path, Value> loader) {
        return new ToolkitCacheBuilder<Path, Value>()
            .withLoader(loader)
            .withReferenceCreator(WeakReference::new)
            .build();
    }

    public static <Value> ToolkitCache<File, Value> newFileToolkitCache(ToolkitCacheLoader<File, Value> loader) {
        return new ToolkitCacheBuilder<File, Value>()
            .withLoader(loader)
            .build();
    }

    public static <Value> ToolkitCache<File, Value> newWeakFileToolkitCache(ToolkitCacheLoader<File, Value> loader) {
        return new ToolkitCacheBuilder<File, Value>()
            .withLoader(loader)
            .withReferenceCreator(WeakReference::new)
            .build();
    }

}
