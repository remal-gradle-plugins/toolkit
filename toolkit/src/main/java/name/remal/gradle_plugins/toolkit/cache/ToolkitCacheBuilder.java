package name.remal.gradle_plugins.toolkit.cache;

import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.cache.ToolkitCacheKeyNormalizer.defaultToolkitCacheKeyNormalizer;
import static name.remal.gradle_plugins.toolkit.cache.ToolkitCacheLastModifiedGetter.defaultToolkitCacheLastModifiedGetter;

import java.lang.ref.SoftReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

@NoArgsConstructor
@Getter
@With
@AllArgsConstructor(access = PRIVATE)
public final class ToolkitCacheBuilder<Key, Value> {

    private ToolkitCacheLoader<Key, Value> loader = __ -> {
        throw new UnsupportedOperationException("Cache loader is not defined");
    };

    private ToolkitCacheKeyNormalizer<Key> keyNormalizer = defaultToolkitCacheKeyNormalizer();

    private ToolkitCacheLastModifiedGetter<Key> lastModifiedTimeGetter = defaultToolkitCacheLastModifiedGetter();

    private ToolkitCacheReferenceCreator<Value> referenceCreator = SoftReference::new;

    private int concurrencyLevel = max(4, getRuntime().availableProcessors() * 2);


    public ToolkitCache<Key, Value> build() {
        return new ToolkitCache<>(
            loader,
            keyNormalizer,
            lastModifiedTimeGetter,
            referenceCreator,
            new ToolkitCacheLocks<>(concurrencyLevel)
        );
    }

}
