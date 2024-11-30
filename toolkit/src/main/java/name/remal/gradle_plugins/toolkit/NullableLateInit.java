package name.remal.gradle_plugins.toolkit;

import javax.annotation.Nullable;

public final class NullableLateInit<T> extends AbstractLateInit<T> {

    public static <T> NullableLateInit<T> nullableLateInit(String name) {
        return new NullableLateInit<>(name);
    }

    public static <T> NullableLateInit<T> nullableLateInit() {
        return new NullableLateInit<>(null);
    }


    private NullableLateInit(@Nullable String name) {
        super(name, true);
    }

}
