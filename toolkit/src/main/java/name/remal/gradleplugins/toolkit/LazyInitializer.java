package name.remal.gradleplugins.toolkit;

import static javax.annotation.meta.When.UNKNOWN;

import com.google.errorprone.annotations.ForOverride;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import lombok.SneakyThrows;

public abstract class LazyInitializer<T> {

    public static <T> LazyInitializer<T> of(Supplier<T> supplier) {
        return new LazyInitializer<T>() {
            @Nonnull(when = UNKNOWN)
            @Override
            protected T create() {
                return supplier.get();
            }
        };
    }


    @Nonnull(when = UNKNOWN)
    @ForOverride
    protected abstract T create() throws Throwable;


    private static final Object NO_INIT = new Object();

    @Nonnull(when = UNKNOWN)
    @SuppressWarnings("unchecked")
    private volatile T object = (T) NO_INIT;

    @SneakyThrows
    @Nonnull(when = UNKNOWN)
    public final T get() {
        if (object == NO_INIT) {
            synchronized (this) {
                if (object == NO_INIT) {
                    object = create();
                }
            }
        }
        return object;
    }

}
