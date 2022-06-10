package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import javax.annotation.CheckForNull;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class SneakyThrowUtils {

    public static RuntimeException sneakyThrow(Throwable exception) {
        return sneakyThrow0(exception);
    }

    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    private static <T extends Throwable> T sneakyThrow0(Throwable exception) throws T {
        throw (T) exception;
    }


    @FunctionalInterface
    public interface SneakyThrowsRunnable {
        void run() throws Throwable;
    }

    public static void sneakyThrows(SneakyThrowsRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable exception) {
            throw sneakyThrow(exception);
        }
    }


    @FunctionalInterface
    public interface SneakyThrowsCallable<V> {
        @CheckForNull
        V call() throws Throwable;
    }

    @CheckForNull
    public static <V> V sneakyThrows(SneakyThrowsCallable<V> callable) {
        try {
            return callable.call();
        } catch (Throwable exception) {
            throw sneakyThrow(exception);
        }
    }

}
