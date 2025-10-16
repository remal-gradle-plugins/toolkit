package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.gradle.api.IsolatedAction;

@NoArgsConstructor(access = PRIVATE)
@MinCompatibleGradleVersion("8.8")
public abstract class IsolatedActionUtils {

    public static <T> IsolatedAction<T> toIsolatedAction(SerializableAction<T> action) {
        return new SerializableActionAdapter<>(action);
    }

    @RequiredArgsConstructor
    @NoArgsConstructor(access = PRIVATE, force = true)
    private static class SerializableActionAdapter<T> implements IsolatedAction<T> {

        private final SerializableAction<T> action;

        @Override
        public void execute(T target) {
            action.execute(target);
        }

    }

}
