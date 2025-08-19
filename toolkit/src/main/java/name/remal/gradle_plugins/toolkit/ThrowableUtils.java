package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class ThrowableUtils {

    public static Throwable unwrapReflectionException(Throwable exception) {
        while (true) {
            Throwable unwrapped = null;
            if (exception instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) exception).getUndeclaredThrowable();

            } else if (exception instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) exception).getTargetException();
            }

            if (unwrapped != null) {
                exception = unwrapped;
            } else {
                break;
            }
        }

        return exception;
    }


    private static final String GUAVA_EXECUTION_ERROR_CLASS_NAME_SUFFIX =
        "!com!google!common!util!concurrent!ExecutionError".replace('!', '.');

    private static final String GUAVA_UNCHECKED_EXECUTION_EXCEPTION_CLASS_NAME_SUFFIX =
        "!com!google!common!util!concurrent!UncheckedExecutionException".replace('!', '.');

    @SuppressWarnings("java:S1871")
    public static Throwable unwrapException(Throwable exception) {
        while (true) {
            exception = unwrapReflectionException(exception);

            Throwable unwrapped = null;
            if (exception instanceof ExecutionException) {
                unwrapped = exception.getCause();

            } else if (exception instanceof UncheckedIOException) {
                unwrapped = exception.getCause();

            } else {
                var dotClassName = '.' + exception.getClass().getName(); // support relocated classes too
                if (dotClassName.endsWith(GUAVA_EXECUTION_ERROR_CLASS_NAME_SUFFIX)) {
                    unwrapped = exception.getCause();
                } else if (dotClassName.endsWith(GUAVA_UNCHECKED_EXECUTION_EXCEPTION_CLASS_NAME_SUFFIX)) {
                    unwrapped = exception.getCause();
                }
            }

            if (unwrapped != null) {
                exception = unwrapped;
            } else {
                break;
            }
        }

        return exception;
    }

}
