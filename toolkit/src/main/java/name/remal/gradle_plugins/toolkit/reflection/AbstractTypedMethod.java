package name.remal.gradle_plugins.toolkit.reflection;

import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.makeAccessible;

import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;

abstract class AbstractTypedMethod {

    protected final Method method;

    protected AbstractTypedMethod(Method method) {
        this.method = makeAccessible(method);
    }

    public final Method getReflectionMethod() {
        return method;
    }

    @Override
    public final String toString() {
        return method.toString();
    }

    @Override
    public final boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof AbstractTypedMethod)) {
            return false;
        }

        AbstractTypedMethod that = (AbstractTypedMethod) other;
        return method.equals(that.method);
    }

    @Override
    public final int hashCode() {
        return method.hashCode();
    }

}
