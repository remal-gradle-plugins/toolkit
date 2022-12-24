package name.remal.gradle_plugins.toolkit;

import static java.lang.Integer.toHexString;
import static java.lang.System.identityHashCode;
import static javax.annotation.meta.When.UNKNOWN;
import static name.remal.gradle_plugins.toolkit.ProxyUtils.isEqualsMethod;
import static name.remal.gradle_plugins.toolkit.ProxyUtils.isHashCodeMethod;
import static name.remal.gradle_plugins.toolkit.ProxyUtils.isToStringMethod;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.invokeDefaultMethod;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import lombok.Value;
import lombok.val;
import org.jetbrains.annotations.Contract;

public final class ProxyInvocationHandler implements InvocationHandler {

    @FunctionalInterface
    public interface MethodInvocationHandler {
        @Nonnull(when = UNKNOWN)
        Object invoke(Object proxy, Method method, @Nonnull(when = UNKNOWN) Object[] args) throws Throwable;
    }

    @Value
    private static class Handler {
        Predicate<Method> predicate;
        MethodInvocationHandler impl;
    }

    private final List<Handler> handlers = new ArrayList<>();

    @Override
    @Nonnull(when = UNKNOWN)
    public Object invoke(Object proxy, Method method, @Nonnull(when = UNKNOWN) Object[] args) throws Throwable {
        for (val handler : handlers) {
            if (handler.getPredicate().test(method)) {
                return handler.getImpl().invoke(proxy, method, args);
            }
        }

        if (isEqualsMethod(method)) {
            return proxy == args[0];
        } else if (isHashCodeMethod(method)) {
            return identityHashCode(proxy);
        } else if (isToStringMethod(method)) {
            return proxy.getClass().getName() + '@' + toHexString(identityHashCode(proxy));
        }

        if (method.isDefault()) {
            return invokeDefaultMethod(method, proxy, args);
        }

        throw new UnsupportedOperationException(method.toString());
    }


    @Contract("_,_->this")
    @CanIgnoreReturnValue
    public ProxyInvocationHandler addFirst(Predicate<Method> predicate, MethodInvocationHandler handler) {
        handlers.add(0, new Handler(predicate, handler));
        return this;
    }

    @Contract("_,_->this")
    @CanIgnoreReturnValue
    public ProxyInvocationHandler add(Predicate<Method> predicate, MethodInvocationHandler handler) {
        handlers.add(new Handler(predicate, handler));
        return this;
    }

}
