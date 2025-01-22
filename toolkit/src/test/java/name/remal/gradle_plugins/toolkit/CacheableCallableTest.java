package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CacheableCallableTest {

    @Test
    void test() throws Throwable {
        var counter = new AtomicInteger(0);
        Callable<Integer> callable = counter::incrementAndGet;
        var cacheableCallable = CacheableCallable.toCacheableCallable(callable);
        assertEquals(1, cacheableCallable.call());
        assertEquals(1, cacheableCallable.call());
    }

}
