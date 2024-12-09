package name.remal.gradle_plugins.toolkit;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Contract;

public final class LazyValue<T> extends LazyValueBase<T> {

    @Contract(pure = true)
    public static <T> LazyValue<T> lazyValue(LazyValueSupplier<T> supplier) {
        return new LazyValue<>(supplier);
    }


    private LazyValue(LazyValueSupplier<T> valueSupplier) {
        super(valueSupplier);
    }

    @Nonnull
    @Override
    public T get() {
        return requireNonNull(super.get());
    }

}
