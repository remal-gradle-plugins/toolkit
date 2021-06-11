package name.remal.gradleplugins.toolkit.testkit.internal.containers;

import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

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
        val key = type;
        return context.getParent()
            .map(this::getStore)
            .map(it -> it.get(key, type))
            .orElse(null);
    }


    @Nullable
    public <T> T getCurrentStoreValue(ExtensionContext context, Class<T> type) {
        val key = type;
        val value = getStore(context).get(key, type);
        if (value == null) {
            return null;
        }

        val parentValue = getParentStoreValue(context, type);
        if (parentValue != value) {
            return value;
        } else {
            return null;
        }
    }


    public <T> T setCurrentStoreValue(ExtensionContext context, T value) {
        val store = getStore(context);
        val key = value.getClass();
        store.put(key, value);
        return value;
    }

    @Nullable
    @SneakyThrows
    public <T> T removeCurrentStoreValue(ExtensionContext context, Class<T> type) {
        val store = getStore(context);
        val key = type;
        val prevValue = store.remove(key, type);
        if (prevValue instanceof CloseableResource) {
            ((CloseableResource) prevValue).close();
        }
        return prevValue;
    }

}
