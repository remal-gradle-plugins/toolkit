package name.remal.gradle_plugins.toolkit;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public final class LazyNullableValue<T> extends LazyValueBase<T> {

    @Contract(pure = true)
    public static <T> LazyNullableValue<T> lazyNullableValue(LazyNullableValueSupplier<T> supplier) {
        return new LazyNullableValue<>(supplier);
    }


    private LazyNullableValue(LazyNullableValueSupplier<T> valueSupplier) {
        super(valueSupplier);
    }

    @Nullable
    @Override
    public T get() {
        return super.get();
    }

}
