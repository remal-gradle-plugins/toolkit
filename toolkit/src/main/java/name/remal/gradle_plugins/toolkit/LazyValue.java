package name.remal.gradle_plugins.toolkit;

import static javax.annotation.meta.When.UNKNOWN;

import com.google.errorprone.annotations.ForOverride;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import lombok.SneakyThrows;
import lombok.val;

public abstract class LazyValue<T> {

    @FunctionalInterface
    public interface LazyValueSupplier<T> {
        @Nonnull(when = UNKNOWN)
        T get() throws Throwable;
    }

    public static <T> LazyValue<T> of(LazyValueSupplier<T> supplier) {
        val supplierHolder = new AtomicReference<>(supplier);
        return new LazyValue<T>() {
            @Override
            @Nonnull(when = UNKNOWN)
            @SneakyThrows
            protected T create() {
                val savedSupplier = supplierHolder.get();
                supplierHolder.set(null);
                return savedSupplier.get();
            }
        };
    }


    /**
     * Use {@link #of(LazyValueSupplier)} instead.
     */
    private LazyValue() {
    }


    @Nonnull(when = UNKNOWN)
    @ForOverride
    protected abstract T create() throws Throwable;


    private static final Object NO_INIT = new Object();

    @Nonnull(when = UNKNOWN)
    @SuppressWarnings("unchecked")
    private volatile T value = (T) NO_INIT;

    @SneakyThrows
    @Nonnull(when = UNKNOWN)
    public final T get() {
        if (value == NO_INIT) {
            synchronized (this) {
                if (value == NO_INIT) {
                    value = create();
                }
            }
        }
        return value;
    }

}
