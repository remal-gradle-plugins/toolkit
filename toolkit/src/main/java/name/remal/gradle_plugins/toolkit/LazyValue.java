package name.remal.gradle_plugins.toolkit;

import static java.util.Objects.requireNonNull;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public final class LazyValue<T> extends LazyValueBase<T> {

    @Contract(pure = true)
    public static <T> LazyValue<T> lazyValue(LazyValueSupplier<T> supplier) {
        return new LazyValue<>(supplier);
    }


    private static final long serialVersionUID = 1L;

    private LazyValue(LazyValueSupplier<T> valueSupplier) {
        super(valueSupplier);
    }

    @NonNull
    @Override
    public T get() {
        return requireNonNull(super.get());
    }

}
