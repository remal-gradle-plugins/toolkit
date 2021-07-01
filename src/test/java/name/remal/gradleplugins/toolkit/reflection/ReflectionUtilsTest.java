package name.remal.gradleplugins.toolkit.reflection;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

class ReflectionUtilsTest {

    @Test
    void tryLoadClass() {
        assertEquals(
            ReflectionUtilsTest.class,
            ReflectionUtils.tryLoadClass(
                ReflectionUtilsTest.class.getName(),
                ReflectionUtilsTest.class.getClassLoader()
            )
        );
        assertEquals(
            ReflectionUtilsTest.class,
            ReflectionUtils.tryLoadClass(
                ReflectionUtilsTest.class.getName()
            )
        );

        assertNull(
            ReflectionUtils.tryLoadClass(
                ReflectionUtilsTest.class.getName() + ":wrong-class-name-suffix",
                ReflectionUtilsTest.class.getClassLoader()
            )
        );
        assertNull(
            ReflectionUtils.tryLoadClass(
                ReflectionUtilsTest.class.getName() + ":wrong-class-name-suffix"
            )
        );
    }

    @Test
    void isClassPresent() {
        assertTrue(
            ReflectionUtils.isClassPresent(
                ReflectionUtilsTest.class.getName(),
                ReflectionUtilsTest.class.getClassLoader()
            )
        );
        assertTrue(
            ReflectionUtils.isClassPresent(
                ReflectionUtilsTest.class.getName()
            )
        );

        assertFalse(
            ReflectionUtils.isClassPresent(
                ReflectionUtilsTest.class.getName() + ":wrong-class-name-suffix",
                ReflectionUtilsTest.class.getClassLoader()
            )
        );
        assertFalse(
            ReflectionUtils.isClassPresent(
                ReflectionUtilsTest.class.getName() + ":wrong-class-name-suffix"
            )
        );
    }


    public static class TestTask extends DefaultTask {
    }

    @Test
    @SuppressWarnings("java:S5845")
    void unwrapGeneratedSubclass(Project project) {
        val task = project.getTasks().create("testTask", TestTask.class);
        val taskType = task.getClass();
        assertNotEquals(TestTask.class, taskType);
        val unwrappedGeneratedSubclass = ReflectionUtils.unwrapGeneratedSubclass(taskType);
        assertEquals(TestTask.class, unwrappedGeneratedSubclass);
    }


    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPES = new LinkedHashMap<>();

    static {
        List<Class<?>> wrapperTypes = asList(
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Void.class
        );
        for (val wrapperType : wrapperTypes) {
            try {
                val primitiveType = (Class<?>) wrapperType.getField("TYPE").get(wrapperType);
                WRAPPER_TO_PRIMITIVE_TYPES.put(wrapperType, primitiveType);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void wrapPrimitiveType() {
        WRAPPER_TO_PRIMITIVE_TYPES.forEach((wrapperType, primitiveType) -> {
            assertEquals(
                wrapperType,
                ReflectionUtils.wrapPrimitiveType(wrapperType),
                primitiveType::getCanonicalName
            );
            assertEquals(
                wrapperType,
                ReflectionUtils.wrapPrimitiveType(primitiveType),
                primitiveType::getCanonicalName
            );
        });

        assertEquals(String.class, ReflectionUtils.wrapPrimitiveType(String.class));
        assertEquals(Collection.class, ReflectionUtils.wrapPrimitiveType(Collection.class));
    }

    @Test
    void unwrapPrimitiveType() {
        WRAPPER_TO_PRIMITIVE_TYPES.forEach((wrapperType, primitiveType) -> {
            assertEquals(
                primitiveType,
                ReflectionUtils.unwrapPrimitiveType(wrapperType),
                primitiveType::getCanonicalName
            );
            assertEquals(
                primitiveType,
                ReflectionUtils.unwrapPrimitiveType(primitiveType),
                primitiveType::getCanonicalName
            );
        });

        assertEquals(String.class, ReflectionUtils.unwrapPrimitiveType(String.class));
        assertEquals(Collection.class, ReflectionUtils.unwrapPrimitiveType(Collection.class));
    }

}
