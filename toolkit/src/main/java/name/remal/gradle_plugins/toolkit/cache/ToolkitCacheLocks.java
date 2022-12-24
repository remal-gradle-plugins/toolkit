package name.remal.gradle_plugins.toolkit.cache;

import static java.lang.Math.abs;

import java.util.concurrent.locks.ReentrantLock;
import lombok.val;

final class ToolkitCacheLocks<Key> {

    private final ReentrantLock[] locks;

    public ToolkitCacheLocks(int concurrencyLevel) {
        if (concurrencyLevel < 1) {
            concurrencyLevel = 1;
        }

        this.locks = new ReentrantLock[concurrencyLevel];
        for (int i = 0; i < concurrencyLevel; ++i) {
            this.locks[i] = new ReentrantLock();
        }
    }

    public ReentrantLock getLock(Key key) {
        val index = abs(key.hashCode() % locks.length);
        return locks[index];
    }

}
