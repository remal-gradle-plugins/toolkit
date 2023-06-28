package name.remal.gradle_plugins.toolkit;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import org.junit.jupiter.api.Test;

class ThrowableUtilsTest {

    @Test
    void unwrapReflectionException() {
        assertThat(ThrowableUtils.unwrapReflectionException(new RuntimeException()))
            .isInstanceOf(RuntimeException.class);
        assertThat(ThrowableUtils.unwrapReflectionException(new RuntimeException(new Throwable())))
            .isInstanceOf(RuntimeException.class);

        assertThat(ThrowableUtils.unwrapReflectionException(new UndeclaredThrowableException(null)))
            .isInstanceOf(UndeclaredThrowableException.class);
        assertThat(ThrowableUtils.unwrapReflectionException(new UndeclaredThrowableException(new Throwable())))
            .isInstanceOf(Throwable.class);

        assertThat(ThrowableUtils.unwrapReflectionException(new InvocationTargetException(null)))
            .isInstanceOf(InvocationTargetException.class);
        assertThat(ThrowableUtils.unwrapReflectionException(new InvocationTargetException(new Throwable())))
            .isInstanceOf(Throwable.class);

        assertThat(ThrowableUtils.unwrapReflectionException(
            new UndeclaredThrowableException(new InvocationTargetException(new Throwable())))
        )
            .isInstanceOf(Throwable.class);

        assertThat(ThrowableUtils.unwrapReflectionException(
            new InvocationTargetException(new UndeclaredThrowableException(new Throwable())))
        )
            .isInstanceOf(Throwable.class);
    }

}
