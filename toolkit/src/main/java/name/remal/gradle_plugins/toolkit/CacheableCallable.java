package name.remal.gradle_plugins.toolkit;

import static javax.annotation.meta.When.UNKNOWN;
import static name.remal.gradle_plugins.toolkit.LazyNullableValue.lazyNullableValue;

import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Contract;

public final class CacheableCallable<V> implements Callable<V> {

    @Contract(pure = true)
    public static <V> Callable<V> toCacheableCallable(Callable<V> delegate) {
        return new CacheableCallable<>(delegate);
    }


    private final LazyNullableValue<V> lazyValue;

    private CacheableCallable(Callable<V> delegate) {
        this.lazyValue = lazyNullableValue(delegate::call);
    }

    @Nonnull(when = UNKNOWN)
    @Override
    public V call() {
        return lazyValue.get();
    }

}
