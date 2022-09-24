package name.remal.gradleplugins.toolkit.reflection;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.isStatic;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.wrapPrimitiveType;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
abstract class MembersFinderHelpers {

    @SneakyThrows
    public static Method getMethod(
        Class<?> type,
        boolean isStatic,
        @Nullable Class<?> returnType,
        String name,
        Class<?>... paramTypes
    ) {
        val method = findMethod(type, isStatic, returnType, name, paramTypes);
        if (method != null) {
            return method;
        }

        val msg = new StringBuilder();
        msg.append("Compatible public ")
            .append(isStatic ? "static" : "non-static")
            .append(" method not found in class ")
            .append(type.getName())
            .append(": ");
        if (returnType != null) {
            msg.append(returnType.getName()).append(" ");
        } else {
            msg.append("<any return type> ");
        }
        msg.append(name).append('(');
        for (int i = 0; i < paramTypes.length; ++i) {
            if (i > 0) {
                msg.append(", ");
            }
            msg.append(paramTypes[i].getName());
        }
        msg.append(')');
        throw new NoSuchMethodException(msg.toString());
    }

    @Nullable
    @SuppressWarnings("java:S3776")
    public static Method findMethod(
        Class<?> type,
        boolean isStatic,
        @Nullable Class<?> returnType,
        String name,
        Class<?>... paramTypes
    ) {
        if (paramTypes.length == 0) {
            return findMethod(type, isStatic, returnType, name);
        }

        val candidateMethods = Stream.of(type.getMethods())
            .filter(method -> isStatic == isStatic(method))
            .filter(method -> method.getParameterCount() == paramTypes.length)
            .filter(method -> returnType == null || isAssignable(method.getReturnType(), returnType))
            .filter(method -> method.getName().equals(name))
            .toArray(Method[]::new);

        forEachMethod:
        for (val method : candidateMethods) {
            for (int i = 0; i < paramTypes.length; ++i) {
                if (paramTypes[i] != method.getParameterTypes()[i]) {
                    continue forEachMethod;
                }
            }
            return method;
        }

        forEachMethod:
        for (val method : candidateMethods) {
            for (int i = 0; i < paramTypes.length; ++i) {
                if (wrapPrimitiveType(paramTypes[i]) != wrapPrimitiveType(method.getParameterTypes()[i])) {
                    continue forEachMethod;
                }
            }
            return method;
        }

        forEachMethod:
        for (val method : candidateMethods) {
            for (int i = 0; i < paramTypes.length; ++i) {
                if (!isAssignable(paramTypes[i], method.getParameterTypes()[i])) {
                    continue forEachMethod;
                }
            }
            return method;
        }

        return null;
    }

    @Nullable
    private static Method findMethod(
        Class<?> type,
        boolean isStatic,
        @Nullable Class<?> returnType,
        String name
    ) {
        final Method method;
        try {
            method = type.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
        if (isStatic != isStatic(method)) {
            return null;
        }
        if (returnType != null && !isAssignable(method.getReturnType(), returnType)) {
            return null;
        }
        return method;
    }

    private static boolean isAssignable(Class<?> fromType, Class<?> toType) {
        if (fromType == toType) {
            return true;
        }
        fromType = wrapPrimitiveType(fromType);
        toType = wrapPrimitiveType(toType);
        return toType.isAssignableFrom(fromType);
    }

}
