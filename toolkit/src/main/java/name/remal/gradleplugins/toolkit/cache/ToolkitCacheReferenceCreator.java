package name.remal.gradleplugins.toolkit.cache;

import java.lang.ref.Reference;

@FunctionalInterface
public interface ToolkitCacheReferenceCreator<Value> {

    Reference<ToolkitCacheItem<Value>> create(ToolkitCacheItem<Value> item) throws Throwable;

}
