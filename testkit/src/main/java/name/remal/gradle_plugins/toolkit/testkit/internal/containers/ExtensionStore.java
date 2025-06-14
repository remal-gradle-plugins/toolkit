package name.remal.gradle_plugins.toolkit.testkit.internal.containers;

import javax.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

@Internal
public class ExtensionStore {

    private final Namespace namespace;

    public ExtensionStore(Extension extension) {
        this.namespace = Namespace.create(extension.getClass());
    }

    public Store getStore(ExtensionContext context) {
        return context.getStore(namespace);
    }


    @Nullable
    public <T> T getParentStoreValue(ExtensionContext context, Class<T> type) {
        var key = type;
        return context.getParent()
            .map(this::getStore)
            .map(it -> it.get(key, type))
            .orElse(null);
    }


    @Nullable
    public <T> T getCurrentStoreValue(ExtensionContext context, Class<T> type) {
        var key = type;
        var value = getStore(context).get(key, type);
        if (value == null) {
            return null;
        }

        var parentValue = getParentStoreValue(context, type);
        if (parentValue != value) {
            return value;
        } else {
            return null;
        }
    }


    public <T> T setCurrentStoreValue(ExtensionContext context, T value) {
        var store = getStore(context);
        var key = value.getClass();
        store.put(key, value);
        return value;
    }

}
