package name.remal.gradle_plugins.toolkit.reflection;

import static javax.annotation.meta.When.UNKNOWN;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;

interface DefaultMethodInvoker {

    @Nonnull(when = UNKNOWN)
    Object invoke(Method method, Object target, Object... args);

}
