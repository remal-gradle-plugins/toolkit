package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("try")
public abstract class AbstractCloseablesContainer implements AutoCloseable {

    @GuardedBy("this")
    private final Deque<AutoCloseable> closeables = new ArrayDeque<>();

    @Contract("_->param1")
    protected synchronized <T extends AutoCloseable> T registerCloseable(T closeable) {
        closeables.addLast(closeable);
        return closeable;
    }

    @Contract("_->param1")
    protected synchronized <T extends Collection<? extends @Nullable AutoCloseable>> T registerCloseables(
        T closeables
    ) {
        closeables.stream()
            .filter(Objects::nonNull)
            .forEach(this::registerCloseable);
        return closeables;
    }


    @Override
    @OverridingMethodsMustInvokeSuper
    @SneakyThrows
    public synchronized void close() {
        List<Throwable> exceptions = new ArrayList<>();

        while (true) {
            var closeable = closeables.pollLast();
            if (closeable == null) {
                break;
            }

            try {
                closeable.close();
            } catch (Throwable exception) {
                exceptions.add(exception);
            }
        }

        if (exceptions.isEmpty()) {
            // do nothing
        } else if (exceptions.size() == 1) {
            throw exceptions.get(0);
        } else {
            throw new CloseablesClosingException(exceptions);
        }
    }


    public static class CloseablesClosingException extends RuntimeException {
        private CloseablesClosingException(Collection<? extends Throwable> exceptions) {
            super(format(
                "Errors occurred while closing %d objects",
                exceptions.size()
            ));

            exceptions.forEach(this::addSuppressed);
        }
    }

}
