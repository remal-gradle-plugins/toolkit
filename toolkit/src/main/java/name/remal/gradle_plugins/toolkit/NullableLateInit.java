package name.remal.gradle_plugins.toolkit;

import javax.annotation.Nullable;
import org.jetbrains.annotations.Contract;

public final class NullableLateInit<T> extends AbstractLateInit<T> {

    @Contract(pure = true)
    public static <T> NullableLateInit<T> nullableLateInit(String name) {
        return new NullableLateInit<>(name);
    }

    @Contract(pure = true)
    public static <T> NullableLateInit<T> nullableLateInit() {
        return new NullableLateInit<>(null);
    }


    private NullableLateInit(@Nullable String name) {
        super(name, true);
    }

}
