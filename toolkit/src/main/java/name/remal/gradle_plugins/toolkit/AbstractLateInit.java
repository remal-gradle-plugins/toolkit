package name.remal.gradle_plugins.toolkit;

import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import lombok.val;

abstract class AbstractLateInit<T> {

    @Nullable
    private final String name;

    private final boolean nullable;

    protected AbstractLateInit(@Nullable String name, boolean nullable) {
        this.name = name;
        this.nullable = nullable;
    }


    private static final Object NOT_INITIALIZED = new Object[0];

    @SuppressWarnings("unchecked")
    private final AtomicReference<T> valueRef = new AtomicReference<>((T) NOT_INITIALIZED);

    @SuppressWarnings("unchecked")
    public void set(@Nullable T value) {
        if (!nullable) {
            if (value == null) {
                throw new IllegalArgumentException(name != null
                    ? name + " can't be set to NULL"
                    : "Can't be set to NULL"
                );
            }
        }

        if (!valueRef.compareAndSet((T) NOT_INITIALIZED, value)) {
            throw new IllegalStateException(name != null
                ? name + " has already been initialized"
                : "Has already been initialized"
            );
        }
    }

    @Nullable
    public T get() {
        val value = valueRef.get();
        if (value == NOT_INITIALIZED) {
            throw new IllegalStateException(name != null && !name.isEmpty()
                ? name + " has NOT been initialized"
                : "LateInit has NOT been initialized"
            );
        }
        return value;
    }

    public boolean isInitialized() {
        return valueRef.get() != NOT_INITIALIZED;
    }

}
