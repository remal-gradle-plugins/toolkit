package name.remal.gradleplugins.toolkit.reflection;

import static name.remal.gradleplugins.toolkit.reflection.WhoCalled.getCallingClass;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import javax.annotation.Nullable;
import lombok.val;
import org.gradle.api.internal.GeneratedSubclass;
import org.jetbrains.annotations.Contract;

public interface ReflectionUtils {

    @Nullable
    static Class<?> tryLoadClass(String name, @Nullable ClassLoader classLoader) {
        try {
            return Class.forName(name, false, classLoader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("java:S109")
    static Class<?> tryLoadClass(String name) {
        val callingClass = getCallingClass(2);
        return tryLoadClass(name, callingClass.getClassLoader());
    }


    static boolean isClassPresent(String name, @Nullable ClassLoader classLoader) {
        return tryLoadClass(name, classLoader) != null;
    }

    @SuppressWarnings("java:S109")
    static boolean isClassPresent(String name) {
        val callingClass = getCallingClass(2);
        return isClassPresent(name, callingClass.getClassLoader());
    }


    @SuppressWarnings("unchecked")
    static <T> Class<T> unwrapGeneratedSubclass(Class<T> type) {
        while (GeneratedSubclass.class.isAssignableFrom(type)) {
            type = (Class<T>) type.getSuperclass();
        }
        return type;
    }


    static Class<?> wrapPrimitiveType(Class<?> type) {
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

    static Class<?> unwrapPrimitiveType(Class<?> type) {
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


    static boolean isPublic(Class<?> type) {
        return Modifier.isPublic(type.getModifiers());
    }

    static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    static boolean isNotPublic(Class<?> type) {
        return !isPublic(type);
    }

    static boolean isNotPublic(Member member) {
        return !isPublic(member);
    }

    static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    static boolean isNotStatic(Member member) {
        return !isStatic(member);
    }


    @Contract("_ -> param1")
    @SuppressWarnings("deprecation")
    static <T extends AccessibleObject & Member> T makeAccessible(T member) {
        if (!member.isAccessible()) {
            if (!isNotPublic(member)
                || !isNotPublic(member.getDeclaringClass())
            ) {
                member.setAccessible(true);
            }
        }
        return member;
    }

}
