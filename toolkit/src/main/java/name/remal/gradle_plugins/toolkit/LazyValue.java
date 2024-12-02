package name.remal.gradle_plugins.toolkit;

import static java.util.Objects.requireNonNull;
import static javax.annotation.meta.When.UNKNOWN;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;

public final class LazyValue<T> {

    @FunctionalInterface
    public interface LazyValueSupplier<T> {
        @Nonnull(when = UNKNOWN)
        T get() throws Throwable;
    }

    public static <T> LazyValue<T> of(LazyValueSupplier<T> supplier) {
        return new LazyValue<>(supplier);
    }


    @Nullable
    private LazyValueSupplier<T> valueSupplier;

    private LazyValue(LazyValueSupplier<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }


    private static final Object NOT_INITIALIZED = new Object[0];

    @Nonnull(when = UNKNOWN)
    @SuppressWarnings("unchecked")
    private volatile T value = (T) NOT_INITIALIZED;

    @SneakyThrows
    @Nonnull(when = UNKNOWN)
    public T get() {
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

    public boolean isInitialized() {
        return value != NOT_INITIALIZED;
    }

    @Override
    public String toString() {
        val value = this.value;
        if (value == NOT_INITIALIZED) {
            return "<not initialized>";
        }

        return String.valueOf(value);
    }

}
