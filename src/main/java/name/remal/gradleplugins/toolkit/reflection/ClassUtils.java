package name.remal.gradleplugins.toolkit.reflection;

import static name.remal.gradleplugins.toolkit.reflection.WhoCalled.getCallingClass;

import javax.annotation.Nullable;
import lombok.val;

public interface ClassUtils {

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

}
