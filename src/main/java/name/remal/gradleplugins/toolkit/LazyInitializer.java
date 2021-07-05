package name.remal.gradleplugins.toolkit;

import lombok.SneakyThrows;

public abstract class LazyInitializer<T> {

    protected abstract T create() throws Throwable;


    private static final Object NO_INIT = new Object();

    @SuppressWarnings("unchecked")
    private volatile T object = (T) NO_INIT;

    @SneakyThrows
    public final T get() {
        if (object == NO_INIT) {
            synchronized (this) {
                if (object == NO_INIT) {
                    object = create();
                }
            }
        }
        return object;
    }

}
