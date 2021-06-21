package name.remal.gradleplugins.toolkit.reflection;

import java.lang.reflect.Method;
import javax.annotation.Nullable;

abstract class AbstractTypedMethod extends AbstractMember {

    protected final Method method;

    protected AbstractTypedMethod(Method method) {
        this.method = makeAccessible(method);
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
