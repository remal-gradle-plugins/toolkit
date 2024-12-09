package name.remal.gradle_plugins.toolkit;

import java.util.Collection;

public class ClosablesContainer extends AbstractClosablesContainer {

    @Override
    public synchronized <T extends AutoCloseable> T registerCloseable(T closeable) {
        return super.registerCloseable(closeable);
    }

    @Override
    public synchronized <T extends Collection<? extends AutoCloseable>> T registerCloseables(T closeables) {
        return super.registerCloseables(closeables);
    }

}
