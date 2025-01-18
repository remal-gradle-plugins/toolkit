package name.remal.gradle_plugins.toolkit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.Contract;

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
    public void set(@Nonnull T value) {
        super.set(value);
    }

    @Nonnull
    @Override
    @SuppressWarnings({"DataFlowIssue", "java:S2638"})
    public T get() {
        return super.get();
    }

}
