package name.remal.gradle_plugins.toolkit;

import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface LazyValueSupplier<T> extends LazyValueSupplierBase<T> {

    @NonNull
    T get() throws Throwable;

}
