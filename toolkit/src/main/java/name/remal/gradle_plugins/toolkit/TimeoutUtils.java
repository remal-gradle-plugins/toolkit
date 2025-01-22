package name.remal.gradle_plugins.toolkit;

import static javax.annotation.meta.When.UNKNOWN;
import static lombok.AccessLevel.PRIVATE;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.SneakyThrowUtils.SneakyThrowsCallable;
import name.remal.gradle_plugins.toolkit.SneakyThrowUtils.SneakyThrowsRunnable;

@NoArgsConstructor(access = PRIVATE)
public abstract class TimeoutUtils {

    @Nonnull(when = UNKNOWN)
    @SneakyThrows
    @SuppressWarnings({"java:S1193", "java:S2142"})
    public static <T> T withTimeout(Duration timeout, SneakyThrowsCallable<T> action) {
        var currentThread = Thread.currentThread();
        var timeoutTriggered = new AtomicBoolean();
        var timeoutThread = new Thread(() -> {
            try {
                Thread.sleep(timeout.toMillis());
                timeoutTriggered.set(true);
                currentThread.interrupt();
            } catch (Throwable ignore) {
                // do nothing
            }
        });
        timeoutThread.setName(TimeoutUtils.class.getSimpleName() + "-timeout");
        timeoutThread.setDaemon(true);
        timeoutThread.start();

        try {
            return action.call();

        } catch (Throwable exception) {
            if (exception instanceof InterruptedException) {
                if (timeoutTriggered.get()) {
                    throw new TimeoutException("Timeout exceeded: " + timeout);
                } else {
                    Thread.currentThread().interrupt();
                }
            }

            throw exception;

        } finally {
            timeoutThread.interrupt();
        }
    }

    public static void withTimeout(Duration timeout, SneakyThrowsRunnable action) {
        withTimeout(timeout, () -> {
            action.run();
            return null;
        });
    }

}
