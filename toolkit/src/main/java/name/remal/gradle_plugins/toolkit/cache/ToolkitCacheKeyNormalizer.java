package name.remal.gradle_plugins.toolkit.cache;

import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import java.io.File;
import java.nio.file.Path;

@FunctionalInterface
public interface ToolkitCacheKeyNormalizer<Key> {

    Key normalizeKey(Key key) throws Throwable;


    default ToolkitCacheKeyNormalizer<Key> then(ToolkitCacheKeyNormalizer<Key> other) {
        return key -> {
            key = this.normalizeKey(key);
            key = other.normalizeKey(key);
            return key;
        };
    }


    @SuppressWarnings("unchecked")
    static <Key> ToolkitCacheKeyNormalizer<Key> defaultToolkitCacheKeyNormalizer() {
        return key -> {
            if (key instanceof Path) {
                return (Key) normalizePath((Path) key);
            } else if (key instanceof File) {
                return (Key) normalizeFile((File) key);
            } else {
                return key;
            }
        };
    }

}
