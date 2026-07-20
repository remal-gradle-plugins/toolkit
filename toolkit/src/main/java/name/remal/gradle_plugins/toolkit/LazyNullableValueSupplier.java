package name.remal.gradle_plugins.toolkit;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface LazyNullableValueSupplier<T> extends LazyValueSupplierBase<T> {

    @Nullable
    @SuppressWarnings("NullAway")
    T get() throws Throwable;

}
