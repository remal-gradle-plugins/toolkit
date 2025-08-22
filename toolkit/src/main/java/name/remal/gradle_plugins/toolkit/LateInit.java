package name.remal.gradle_plugins.toolkit;

import static java.util.Objects.requireNonNull;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class LateInit<T> extends AbstractLateInit<T> {

    @Contract(pure = true)
    public static <T> LateInit<T> lateInit(String name) {
        return new LateInit<>(name);
    }

    @Contract(pure = true)
    public static <T> LateInit<T> lateInit() {
        return new LateInit<>(null);
    }


    private LateInit(@Nullable String name) {
        super(name, false);
    }

    @Override
    @SuppressWarnings({"NullableProblems", "java:S2638"})
    public void set(@NonNull T value) {
        super.set(value);
    }

    @NonNull
    @Override
    @SuppressWarnings("java:S2638")
    public T get() {
        return requireNonNull(super.get());
    }

}
