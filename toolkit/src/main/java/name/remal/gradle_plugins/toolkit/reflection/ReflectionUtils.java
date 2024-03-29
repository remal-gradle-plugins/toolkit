package name.remal.gradle_plugins.toolkit.reflection;

import static java.beans.Introspector.decapitalize;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.write;
import static javax.annotation.meta.When.UNKNOWN;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;
import static name.remal.gradle_plugins.toolkit.DebugUtils.ifDebugEnabled;
import static name.remal.gradle_plugins.toolkit.reflection.WhoCalledUtils.getCallingClass;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.internal.GeneratedSubclass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;

@NoArgsConstructor(access = PRIVATE)
@CustomLog
public abstract class ReflectionUtils {

    @Nullable
    public static Class<?> tryLoadClass(String name, @Nullable ClassLoader classLoader) {
        try {
            return Class.forName(name, false, classLoader);

        } catch (ClassNotFoundException expected) {
            // do nothing
        }

        return null;
    }

    @Nullable
    @SuppressWarnings("java:S109")
    public static Class<?> tryLoadClass(String name) {
        val callingClass = getCallingClass(2);
        return tryLoadClass(name, callingClass.getClassLoader());
    }


    public static boolean isClassPresent(String name, @Nullable ClassLoader classLoader) {
        return tryLoadClass(name, classLoader) != null;
    }

    @SuppressWarnings("java:S109")
    public static boolean isClassPresent(String name) {
        val callingClass = getCallingClass(2);
        return isClassPresent(name, callingClass.getClassLoader());
    }

    @SneakyThrows
    public static Class<?> defineClass(ClassLoader classLoader, byte[] bytecode) {
        ifDebugEnabled(() -> {
            val className = new ClassReader(bytecode).getClassName().replace('/', '.');
            val tempFile = createTempFile(className + '-', ".class");
            write(tempFile, bytecode);
        });

        try {
            val defineClassMethod = ClassLoader.class.getDeclaredMethod(
                "defineClass",
                String.class,
                byte[].class,
                int.class,
                int.class
            );
            makeAccessible(defineClassMethod);
            return (Class<?>) defineClassMethod.invoke(classLoader, null, bytecode, 0, bytecode.length);

        } catch (Exception exception) {
            val className = new ClassReader(bytecode).getClassName().replace('/', '.');
            throw new RuntimeException("Class defining error: " + className, exception);
        }
    }


    @Unmodifiable
    public static List<Class<?>> getClassHierarchy(Class<?> rootClass) {
        Set<Class<?>> result = new LinkedHashSet<>();
        result.add(rootClass);

        Deque<Class<?>> queue = new ArrayDeque<>();
        queue.addLast(rootClass);
        while (true) {
            val clazz = queue.pollFirst();
            if (clazz == null) {
                break;
            }

            val superClass = clazz.getSuperclass();
            if (superClass != null) {
                if (result.add(superClass)) {
                    queue.addLast(superClass);
                }
            }

            for (val interfaceClass : clazz.getInterfaces()) {
                if (result.add(interfaceClass)) {
                    queue.addLast(interfaceClass);
                }
            }
        }

        return ImmutableList.copyOf(result);
    }


    @ReliesOnInternalGradleApi
    @SuppressWarnings("unchecked")
    public static <T> Class<T> unwrapGeneratedSubclass(Class<T> type) {
        while (GeneratedSubclass.class.isAssignableFrom(type)) {
            type = (Class<T>) type.getSuperclass();
        }
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> classOf(T object) {
        return (Class<T>) unwrapGeneratedSubclass(object.getClass());
    }

    public static String packageNameOf(Class<?> clazz) {
        val className = clazz.getName();
        int lastDelimPos = className.lastIndexOf('.');
        return lastDelimPos >= 0 ? className.substring(0, lastDelimPos) : "";
    }


    public static Class<?> wrapPrimitiveType(Class<?> type) {
        if (boolean.class == type) {
            return Boolean.class;
        } else if (char.class == type) {
            return Character.class;
        } else if (byte.class == type) {
            return Byte.class;
        } else if (short.class == type) {
            return Short.class;
        } else if (int.class == type) {
            return Integer.class;
        } else if (long.class == type) {
            return Long.class;
        } else if (float.class == type) {
            return Float.class;
        } else if (double.class == type) {
            return Double.class;
        } else if (void.class == type) {
            return Void.class;
        }
        return type;
    }

    public static Class<?> unwrapPrimitiveType(Class<?> type) {
        if (Boolean.class == type) {
            return boolean.class;
        } else if (Character.class == type) {
            return char.class;
        } else if (Byte.class == type) {
            return byte.class;
        } else if (Short.class == type) {
            return short.class;
        } else if (Integer.class == type) {
            return int.class;
        } else if (Long.class == type) {
            return long.class;
        } else if (Float.class == type) {
            return float.class;
        } else if (Double.class == type) {
            return double.class;
        } else if (Void.class == type) {
            return void.class;
        }
        return type;
    }


    public static boolean isPublic(Class<?> type) {
        return Modifier.isPublic(type.getModifiers());
    }

    public static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    public static boolean isAbstract(Class<?> type) {
        return Modifier.isAbstract(type.getModifiers());
    }

    public static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    public static boolean isNotPublic(Class<?> type) {
        return !isPublic(type);
    }

    public static boolean isNotPublic(Member member) {
        return !isPublic(member);
    }

    public static boolean isNotStatic(Member member) {
        return !isStatic(member);
    }

    public static boolean isNotAbstract(Class<?> type) {
        return !isAbstract(type);
    }

    public static boolean isNotAbstract(Method method) {
        return !isAbstract(method);
    }


    private static final Pattern GETTER_NAME = Pattern.compile("^get[^a-z\\p{Ll}].*$");
    private static final Pattern BOOLEAN_GETTER_NAME = Pattern.compile("^is[^a-z\\p{Ll}].*$");

    @Nullable
    private static final Class<?> RECORD_CLASS = tryLoadClass("java.lang.Record");

    public static boolean isGetter(Method method) {
        if (isStatic(method)) {
            return false;
        }

        if (method.getParameterCount() != 0) {
            return false;
        }

        val returnType = method.getReturnType();
        if (returnType == void.class) {
            return false;
        }

        if (RECORD_CLASS != null && RECORD_CLASS.isAssignableFrom(method.getDeclaringClass())) {
            return true;
        }

        if (returnType == boolean.class) {
            return BOOLEAN_GETTER_NAME.matcher(method.getName()).matches();
        }

        return GETTER_NAME.matcher(method.getName()).matches();
    }

    public static boolean isGetterOf(Method method, Class<?> type) {
        return isGetter(method) && type.isAssignableFrom(method.getReturnType());
    }

    public static String getPropertyNameForGetter(Method method) {
        if (!isGetter(method)) {
            throw new AssertionError("Not a getter: " + method);
        }

        val methodName = method.getName();

        if (RECORD_CLASS != null && RECORD_CLASS.isAssignableFrom(method.getDeclaringClass())) {
            return methodName;
        }

        if (methodName.startsWith("get")) {
            return decapitalize(methodName.substring("get".length()));
        } else if (methodName.startsWith("is")) {
            return decapitalize(methodName.substring("is".length()));
        } else {
            return methodName;
        }
    }


    @Contract("_ -> param1")
    @SuppressWarnings({"deprecation", "java:S3011"})
    public static <T extends AccessibleObject & Member> T makeAccessible(T member) {
        if (!member.isAccessible()) {
            if (isNotPublic(member) || isNotPublic(member.getDeclaringClass())) {
                member.setAccessible(true);
            }
        }
        return member;
    }


    private static final DefaultMethodInvoker DEFAULT_METHOD_INVOKER
        = loadCrossCompileService(DefaultMethodInvoker.class);

    @Nonnull(when = UNKNOWN)
    public static Object invokeDefaultMethod(Method method, Object target, @Nullable Object... args) {
        if (!method.isDefault()) {
            throw new IllegalArgumentException("Not a default method: " + method);
        }
        if (args == null) {
            return DEFAULT_METHOD_INVOKER.invoke(method, target);
        } else {
            return DEFAULT_METHOD_INVOKER.invoke(method, target, args);
        }
    }

}
