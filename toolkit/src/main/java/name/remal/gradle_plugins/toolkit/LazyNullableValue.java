package name.remal.gradle_plugins.toolkit;

import javax.annotation.Nullable;
import org.jetbrains.annotations.Contract;

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
