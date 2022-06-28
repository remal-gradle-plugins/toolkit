package name.remal.gradleplugins.toolkit.reflection;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;
import static javax.annotation.meta.When.UNKNOWN;
import static lombok.AccessLevel.PUBLIC;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PUBLIC)
@SuppressWarnings("unused")
final class DefaultMethodInvokerPrivateLookup implements DefaultMethodInvoker {

    @Nonnull(when = UNKNOWN)
    @Override
    @SneakyThrows
    public Object invoke(Method method, Object target, Object... args) {
        val lookup = privateLookupIn(method.getDeclaringClass(), lookup());
        return lookup
            .in(method.getDeclaringClass())
            .unreflectSpecial(method, method.getDeclaringClass())
            .bindTo(target)
            .invokeWithArguments(args);
    }

}
