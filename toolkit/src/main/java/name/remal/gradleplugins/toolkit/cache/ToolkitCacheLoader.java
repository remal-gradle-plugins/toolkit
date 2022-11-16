package name.remal.gradleplugins.toolkit.cache;

@FunctionalInterface
public interface ToolkitCacheLoader<Key, Value> {

    Value load(Key key) throws Throwable;

}
