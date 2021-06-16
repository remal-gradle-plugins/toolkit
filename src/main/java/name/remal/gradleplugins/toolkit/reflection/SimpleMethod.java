package name.remal.gradleplugins.toolkit.reflection;

import static javax.annotation.meta.When.UNKNOWN;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

@EqualsAndHashCode(of = "method")
public class SimpleMethod extends SimpleMember {

    private final Method method;

    public SimpleMethod(Method method) {
        this.method = makeAccessible(method);
    }

    @Override
    public String toString() {
        return method.toString();
    }

    @Nonnull(when = UNKNOWN)
    @SneakyThrows
    public Object invoke(Object target, Object... params) {
        return method.invoke(target, params);
    }

}
