package name.remal.gradle_plugins.toolkit.cache.files;

import org.jspecify.annotations.Nullable;

public interface ToolkitFilesCacheValue {

    @Nullable
    <T> T get(ToolkitFilesCacheField<T> field);

    <T> ToolkitFilesCacheValue set(ToolkitFilesCacheField<T> field, @Nullable T value);

}
