package name.remal.gradleplugins.toolkit.reflection;

import static javax.annotation.meta.When.UNKNOWN;
import static lombok.AccessLevel.PUBLIC;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.makeAccessible;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PUBLIC)
@SuppressWarnings("unused")
final class DefaultMethodInvokerFallback implements DefaultMethodInvoker {

    @Nonnull(when = UNKNOWN)
    @Override
    @SneakyThrows
    public Object invoke(Method method, Object target, Object... args) {
        val lookup = makeAccessible(Lookup.class.getDeclaredConstructor(Class.class))
            .newInstance(method.getDeclaringClass());
        return lookup
            .in(method.getDeclaringClass())
            .unreflectSpecial(method, method.getDeclaringClass())
            .bindTo(target)
            .invokeWithArguments(args);
    }

}
