package name.remal.gradle_plugins.toolkit;

import static java.util.Objects.requireNonNull;
import static javax.annotation.meta.When.UNKNOWN;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.google.errorprone.annotations.concurrent.LazyInit;
import javax.annotation.Nonnull;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

abstract class LazyValueBase<T> {

    @Nullable
    private LazyValueSupplierBase<T> valueSupplier;

    protected LazyValueBase(LazyValueSupplierBase<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }


    private static final Object NOT_INITIALIZED = new Object[0];

    @Nonnull(when = UNKNOWN)
    @LazyInit
    @SuppressWarnings("unchecked")
    private volatile T value = (T) NOT_INITIALIZED;

    @Nonnull(when = UNKNOWN)
    @SneakyThrows
    @OverridingMethodsMustInvokeSuper
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
        var value = this.value;
        if (value == NOT_INITIALIZED) {
            return "<not initialized>";
        }

        return String.valueOf(value);
    }

}
