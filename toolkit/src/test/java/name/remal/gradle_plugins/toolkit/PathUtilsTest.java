package name.remal.gradle_plugins.toolkit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static name.remal.gradle_plugins.toolkit.SneakyThrowUtils.sneakyThrowsRunnable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

class PathUtilsTest {

    @Test
    @SuppressWarnings({"resource", "java:S2925"})
    void withShortExclusiveLock(@TempDir(cleanup = CleanupMode.ALWAYS) Path tempDir) throws Throwable {
        var lockFilePath = tempDir.resolve("dir/subdir/lockfile.lock");

        var counter = new AtomicInteger();
        var threads = 8;
        var iterations = 100;

        var executor = newFixedThreadPool(threads);
        try {
            var futures = new ArrayList<Future<?>>();
            var start = new CyclicBarrier(threads);
            for (var thread = 1; thread <= threads; thread++) {
                futures.add(executor.submit(sneakyThrowsRunnable(() -> {
                    start.await();

                    for (var iteration = 1; iteration <= iterations; iteration++) {
                        PathUtils.withShortExclusiveLock(lockFilePath, () -> {
                            var value = counter.get();
                            Thread.sleep(1); // Simulate some work
                            counter.set(value + 1);
                        });
                    }
                })));
            }

            for (var future : futures) {
                future.get(30, SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertEquals(threads * iterations, counter.get());
    }

}
