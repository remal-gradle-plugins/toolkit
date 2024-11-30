package name.remal.gradle_plugins.toolkit;

import java.util.concurrent.Callable;

public final class CacheableCallable<V> implements Callable<V> {

    public static <V> Callable<V> toCacheableCallable(Callable<V> delegate) {
        return new CacheableCallable<>(delegate);
    }


    private final LazyValue<V> lazyValue;

    private CacheableCallable(Callable<V> delegate) {
        this.lazyValue = LazyValue.of(delegate::call);
    }

    @Override
    public V call() {
        return lazyValue.get();
    }

}
