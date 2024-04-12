package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.val;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S2925")
class TimeoutUtilsTest {

    @Test
    void normal() {
        assertDoesNotThrow(() ->
            TimeoutUtils.withTimeout(Duration.ofMillis(100), () -> {
                // do nothing
            })
        );
    }

    @Test
    void generalException() {
        assertThrows(IOException.class, () ->
            TimeoutUtils.withTimeout(Duration.ofMillis(100), () -> {
                throw new IOException("test");
            })
        );
    }

    @Test
    void interruptedException() {
        val currentThread = Thread.currentThread();
        assertThrows(InterruptedException.class, () ->
            TimeoutUtils.withTimeout(Duration.ofMillis(100), () -> {
                new Thread(currentThread::interrupt).start();
                Thread.sleep(50);
            })
        );
    }

    @Test
    void timeout() {
        assertThrows(TimeoutException.class, () ->
            TimeoutUtils.withTimeout(Duration.ofMillis(100), () -> {
                Thread.sleep(200);
            })
        );
    }

}
