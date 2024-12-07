package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.val;
import org.jetbrains.annotations.Contract;

@SuppressWarnings("try")
public class ClosablesContainer implements AutoCloseable {

    private final Deque<AutoCloseable> closeables = new ArrayDeque<>();

    @Contract("_->param1")
    @Synchronized
    protected <T extends AutoCloseable> T registerCloseable(T closeable) {
        closeables.addLast(closeable);
        return closeable;
    }

    @Contract("_->param1")
    @Synchronized
    protected <T extends AutoCloseable, C extends Collection<T>> C registerCloseables(C closeables) {
        closeables.stream()
            .filter(Objects::nonNull)
            .forEach(this::registerCloseable);
        return closeables;
    }


    @Override
    @Synchronized
    @OverridingMethodsMustInvokeSuper
    @SneakyThrows
    public void close() {
        List<Throwable> exceptions = new ArrayList<>();

        while (true) {
            val closeable = closeables.pollLast();
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
            throw new ClosablesClosureException(exceptions);
        }
    }


    public static class ClosablesClosureException extends RuntimeException {
        private ClosablesClosureException(Collection<? extends Throwable> exceptions) {
            super(format(
                "Errors occurred while closing %d objects",
                exceptions.size()
            ));

            exceptions.forEach(this::addSuppressed);
        }
    }

}
