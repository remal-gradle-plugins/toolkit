package name.remal.gradleplugins.toolkit;

import java.util.concurrent.Callable;

public final class CacheableCallable<V> implements Callable<V> {

    public static <V> Callable<V> toCacheableCallable(Callable<V> delegate) {
        return new CacheableCallable<>(delegate);
    }


    private final LazyInitializer<V> lazyInitializer;

    private CacheableCallable(Callable<V> delegate) {
        this.lazyInitializer = new LazyInitializer<V>() {
            @Override
            protected V create() throws Throwable {
                return delegate.call();
            }
        };
    }

    @Override
    public V call() {
        return lazyInitializer.get();
    }

}
