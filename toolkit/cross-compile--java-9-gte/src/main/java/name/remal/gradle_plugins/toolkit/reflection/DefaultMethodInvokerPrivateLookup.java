package name.remal.gradle_plugins.toolkit.reflection;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;
import static javax.annotation.meta.When.UNKNOWN;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import lombok.SneakyThrows;
import lombok.val;

@AutoService(DefaultMethodInvoker.class)
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
