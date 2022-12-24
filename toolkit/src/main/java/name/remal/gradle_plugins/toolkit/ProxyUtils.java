package name.remal.gradle_plugins.toolkit;

import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class ProxyUtils {

    public static boolean isEqualsMethod(Method method) {
        return boolean.class == method.getReturnType()
            && method.getParameterCount() == 1
            && method.getName().equals("equals");
    }

    public static boolean isHashCodeMethod(Method method) {
        return int.class == method.getReturnType()
            && method.getParameterCount() == 0
            && method.getName().equals("hashCode");
    }

    public static boolean isToStringMethod(Method method) {
        return String.class == method.getReturnType()
            && method.getParameterCount() == 0
            && method.getName().equals("toString");
    }


    public static <T> T toDynamicInterface(Object object, Class<T> interfaceClass) {
        requireNonNull(object, "object");
        requireNonNull(interfaceClass, "interfaceClass");
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Not an interface:" + interfaceClass);
        }

        Map<Method, Method> interfaceToObjectMethods = new LinkedHashMap<>();
        for (val interfaceMethod : interfaceClass.getMethods()) {
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

        val proxyInstance = newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            new ProxyInvocationHandler().add(interfaceToObjectMethods::containsKey, (proxy, method, args) -> {
                val objectMethod = requireNonNull(interfaceToObjectMethods.get(method));
                return objectMethod.invoke(object, args);
            })
        );

        return interfaceClass.cast(proxyInstance);
    }

}
