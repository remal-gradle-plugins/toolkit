package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isIndependentClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isRecord;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleManagedTypeUtils {

    public static boolean isGradleManagedType(Class<?> clazz) {
        if (clazz.isPrimitive()
            || clazz.isArray()
            || clazz.isAnnotation()
            || clazz.isEnum()
            || isRecord(clazz)
        ) {
            return false;
        }

        if (!clazz.isInterface() && !isIndependentClass(clazz)) {
            return false;
        }

        throw new UnsupportedOperationException();
    }

}
