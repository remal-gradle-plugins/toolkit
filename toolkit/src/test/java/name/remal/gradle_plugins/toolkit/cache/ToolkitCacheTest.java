package name.remal.gradle_plugins.toolkit.cache;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.write;
import static name.remal.gradle_plugins.toolkit.PathUtils.deleteRecursively;
import static name.remal.gradle_plugins.toolkit.cache.ToolkitCaches.newPathToolkitCache;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ToolkitCacheTest {

    Path tempDir;

    @BeforeEach
    void beforeEach() throws Throwable {
        tempDir = createTempDirectory(ToolkitCacheTest.class.getSimpleName() + "-");
    }

    @AfterEach
    void afterEach() {
        deleteRecursively(tempDir);
    }


    @Test
    void exists() throws Throwable {
        val counter = new AtomicInteger();
        val pathCache = newPathToolkitCache(__ -> counter.incrementAndGet());

        val path = tempDir.resolve("exists");
        createFile(path);
        assertEquals(1, pathCache.get(path));
        assertEquals(1, pathCache.get(path));
    }

    @Test
    void doesNotExist() {
        val counter = new AtomicInteger();
        val pathCache = newPathToolkitCache(__ -> counter.incrementAndGet());

        val path = tempDir.resolve("doesNotExist");
        assertEquals(1, pathCache.get(path));
        assertEquals(1, pathCache.get(path));
    }

    @Test
    @SuppressWarnings({"java:S2925", "BusyWait"})
    void changedLastModified() throws Throwable {
        val counter = new AtomicInteger();
        val pathCache = newPathToolkitCache(__ -> counter.incrementAndGet());

        val path = tempDir.resolve("file");
        write(path, String.valueOf(System.nanoTime()).getBytes(UTF_8));
        assertEquals(1, pathCache.get(path));
        assertEquals(1, pathCache.get(path));

        val initialLastModified = getLastModifiedTime(path);
        do {
            Thread.sleep(10);
            write(path, String.valueOf(System.nanoTime()).getBytes(UTF_8));
        } while (getLastModifiedTime(path).equals(initialLastModified));

        assertEquals(2, pathCache.get(path));
        assertEquals(2, pathCache.get(path));
    }

}
