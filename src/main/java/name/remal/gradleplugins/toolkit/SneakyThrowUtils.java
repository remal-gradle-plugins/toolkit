package name.remal.gradleplugins.toolkit;

import javax.annotation.CheckForNull;

public abstract class SneakyThrowUtils {

    public static RuntimeException sneakyThrow(Throwable exception) {
        return sneakyThrow0(exception);
    }

    @SuppressWarnings("unchecked")
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


    private SneakyThrowUtils() {
    }

}
