package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
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

}
