package name.remal.gradleplugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.val;
import org.junit.jupiter.api.Test;

class CacheableCallableTest {

    @Test
    void test() throws Throwable {
        val counter = new AtomicInteger(0);
        Callable<Integer> callable = counter::incrementAndGet;
        val cacheableCallable = CacheableCallable.toCacheableCallable(callable);
        assertEquals(1, cacheableCallable.call());
        assertEquals(1, cacheableCallable.call());
    }

}
