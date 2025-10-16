package name.remal.gradle_plugins.toolkit;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ThrowableUtils.unwrapReflectionException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.Action;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class ProxyUtils {

    @Contract(pure = true)
    @SuppressWarnings("ReferenceEquality")
    public static boolean areMethodsSimilar(@Nullable Method method1, @Nullable Method method2) {
        if (method1 == method2) {
            return true;
        } else if (method1 == null || method2 == null) {
            return false;
        }

        return Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes())
            && method1.getName().equals(method2.getName());
    }


    private static final Method EQUALS_METHOD = getMethod(Object.class, "equals", Object.class);

    @Contract(pure = true)
    public static boolean isEqualsMethod(Method method) {
        return areMethodsSimilar(method, EQUALS_METHOD);
    }

    private static final Method HASH_CODE_METHOD = getMethod(Object.class, "hashCode");

    @Contract(pure = true)
    public static boolean isHashCodeMethod(Method method) {
        return areMethodsSimilar(method, HASH_CODE_METHOD);
    }

    private static final Method TO_STRING_METHOD = getMethod(Object.class, "toString");

    @Contract(pure = true)
    public static boolean isToStringMethod(Method method) {
        return areMethodsSimilar(method, TO_STRING_METHOD);
    }


    @Contract(pure = true)
    public static <T> T toDynamicInterface(
        Object object,
        Class<T> interfaceClass
    ) {
        return toDynamicInterface(object, interfaceClass, __ -> { });
    }

    @Contract(pure = true)
    public static <T> T toDynamicInterface(
        Object object,
        Class<T> interfaceClass,
        Action<? super ProxyInvocationHandler> invocationHandlerConfigurer
    ) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Not an interface:" + interfaceClass);
        }

        Map<Method, Method> interfaceToObjectMethods = new LinkedHashMap<>();
        for (var interfaceMethod : interfaceClass.getMethods()) {
            final Method objectMethod;
            try {
                objectMethod = object.getClass().getMethod(
                    interfaceMethod.getName(),
                    interfaceMethod.getParameterTypes()
                );
            } catch (NoSuchMethodException expected) {
                continue;
            }

            interfaceToObjectMethods.put(interfaceMethod, objectMethod);
        }

        var invocationHandler = new ProxyInvocationHandler();
        invocationHandler.add(interfaceToObjectMethods::containsKey, (proxy, method, args) -> {
            var objectMethod = requireNonNull(interfaceToObjectMethods.get(method));
            try {
                return objectMethod.invoke(object, args);
            } catch (Throwable e) {
                throw unwrapReflectionException(e);
            }
        });

        invocationHandlerConfigurer.execute(invocationHandler);

        var proxyInstance = newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            invocationHandler
        );

        return interfaceClass.cast(proxyInstance);
    }


    @SneakyThrows
    private static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        return clazz.getMethod(name, parameterTypes);
    }

}
