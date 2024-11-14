package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.gradle.api.Action;

@NoArgsConstructor(access = PRIVATE)
public abstract class ActionUtils {

    private static final Action<?> DO_NOTHING_ACTION = __ -> { };

    @SuppressWarnings("unchecked")
    public static <T> Action<T> doNothingAction() {
        return (Action<T>) DO_NOTHING_ACTION;
    }

}
