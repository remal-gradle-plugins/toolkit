package name.remal.gradle_plugins.toolkit;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface LazyValueSupplier<T> extends LazyValueSupplierBase<T> {

    @Nonnull
    T get() throws Throwable;

}
