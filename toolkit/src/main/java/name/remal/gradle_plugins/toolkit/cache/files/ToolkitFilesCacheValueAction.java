package name.remal.gradle_plugins.toolkit.cache.files;

@FunctionalInterface
public interface ToolkitFilesCacheValueAction {

    void execute(ToolkitFilesCacheValue value) throws Throwable;

}
