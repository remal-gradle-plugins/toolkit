package name.remal.gradleplugins.toolkit.cache;

import static name.remal.gradleplugins.toolkit.FileUtils.getFileLastModifiedIfExists;
import static name.remal.gradleplugins.toolkit.PathUtils.getPathLastModifiedIfExists;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import javax.annotation.Nullable;

@FunctionalInterface
public interface ToolkitCacheLastModifiedGetter<Key> {

    @Nullable
    FileTime getLastModified(Key key) throws Throwable;


    default ToolkitCacheLastModifiedGetter<Key> then(ToolkitCacheLastModifiedGetter<Key> other) {
        return key -> {
            FileTime result = this.getLastModified(key);
            if (result == null) {
                result = other.getLastModified(key);
            }
            return result;
        };
    }


    static <Key> ToolkitCacheLastModifiedGetter<Key> defaultToolkitCacheLastModifiedGetter() {
        return key -> {
            if (key instanceof Path) {
                return getPathLastModifiedIfExists((Path) key);
            } else if (key instanceof File) {
                return getFileLastModifiedIfExists((File) key);
            } else {
                return null;
            }
        };
    }

}
