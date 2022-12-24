package name.remal.gradle_plugins.toolkit.cache;

@FunctionalInterface
public interface ToolkitCacheLoader<Key, Value> {

    Value load(Key key) throws Throwable;

}
