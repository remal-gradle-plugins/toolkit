package name.remal.gradle_plugins.toolkit;

import static java.util.Objects.requireNonNull;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("java:S1948")
abstract class LazyValueBase<T> implements Serializable {

    private static final long serialVersionUID = 1L;


    @Nullable
    private transient LazyValueSupplierBase<T> valueSupplier;

    protected LazyValueBase(LazyValueSupplierBase<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }


    private static final Object NOT_INITIALIZED = new Object[0];

    @Nullable
    @LazyInit
    @SuppressWarnings("unchecked")
    private volatile T value = (T) NOT_INITIALIZED;

    @Nullable
    @OverridingMethodsMustInvokeSuper
    @SneakyThrows
    protected T get() {
        if (value == NOT_INITIALIZED) {
            synchronized (this) {
                if (value == NOT_INITIALIZED) {
                    value = requireNonNull(valueSupplier).get();
                    valueSupplier = null;
                }
            }
        }
        return value;
    }

    public final boolean isInitialized() {
        return value != NOT_INITIALIZED;
    }

    @Override
    public final String toString() {
        Object value = this.value;
        if (value == NOT_INITIALIZED) {
            return "<not initialized>";
        }

        return String.valueOf(value);
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        get();
        out.defaultWriteObject();
    }

}
