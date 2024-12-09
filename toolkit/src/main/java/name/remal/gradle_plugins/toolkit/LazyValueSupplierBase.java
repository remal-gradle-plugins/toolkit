package name.remal.gradle_plugins.toolkit;

import static javax.annotation.meta.When.UNKNOWN;

import javax.annotation.Nonnull;

@FunctionalInterface
interface LazyValueSupplierBase<T> {

    @Nonnull(when = UNKNOWN)
    T get() throws Throwable;

}
