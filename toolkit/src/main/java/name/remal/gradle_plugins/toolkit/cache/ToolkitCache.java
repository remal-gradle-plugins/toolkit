package name.remal.gradle_plugins.toolkit.cache;

import static lombok.AccessLevel.PACKAGE;

import java.lang.ref.Reference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor(access = PACKAGE)
public final class ToolkitCache<Key, Value> {

    private final ConcurrentMap<Key, Reference<ToolkitCacheItem<Value>>> map = new ConcurrentHashMap<>();

    private final ToolkitCacheLoader<Key, Value> loader;
    private final ToolkitCacheKeyNormalizer<Key> keyNormalizer;
    private final ToolkitCacheLastModifiedGetter<Key> lastModifiedTimeGetter;
    private final ToolkitCacheReferenceCreator<Value> referenceCreator;
    private final ToolkitCacheLocks<Key> locks;

    public void cleanup() {
        map.values().removeIf(ref -> ref.get() == null);
    }

    @SneakyThrows
    public Value get(Key key) {
        cleanup();

        key = keyNormalizer.normalizeKey(key);
        var lastModified = lastModifiedTimeGetter.getLastModified(key);

        var cachedItemRef = map.get(key);
        if (cachedItemRef != null) {
            var cachedItem = cachedItemRef.get();
            if (cachedItem != null) {
                var cachedLastModified = cachedItem.getLastModified();
                if (Objects.equals(cachedLastModified, lastModified)) {
                    return cachedItem.getValue();
                }
            }
        }

        var lock = locks.getLock(key);
        lock.lock();
        try {
            var value = loader.load(key);
            var item = new ToolkitCacheItem<>(lastModified, value);
            var itemRef = referenceCreator.create(item);
            map.put(key, itemRef);
            return value;

        } finally {
            lock.unlock();
        }
    }

}
