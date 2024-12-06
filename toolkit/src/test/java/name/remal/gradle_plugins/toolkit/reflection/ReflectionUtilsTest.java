package name.remal.gradle_plugins.toolkit.reflection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractList;
import java.util.ArrayList;
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


    @Test
    void packageNameOf() {
        assertEquals(
            "java.lang",
            ReflectionUtils.packageNameOf(String.class)
        );
        assertEquals(
            ReflectionUtils.packageNameOf(ReflectionUtilsTest.class),
            ReflectionUtils.packageNameOf(TestTask.class)
        );
    }


    @Test
    void moduleNameOf() {
        assertEquals(
            "java.base",
            ReflectionUtils.moduleNameOf(String.class)
        );

        assertNull(
            ReflectionUtils.moduleNameOf(ReflectionUtilsTest.class)
        );
    }

    @Test
    void moduleNameOfJdkClass() {
        assertEquals(
            "java.base",
            ReflectionUtils.moduleNameOfJdkClass(String.class.getName())
        );
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


    @Test
    void iterateClassHierarchyWithoutInterfaces() {
        val result = ReflectionUtils.iterateClassHierarchyWithoutInterfaces(ArrayList.class);
        assertThat(result).containsSubsequence(
            ArrayList.class,
            AbstractList.class,
            Object.class
        );
    }


    @Test
    void hierarchyContainsSelf() {
        assertTrue(ReflectionUtils.getClassHierarchy(ChildClass.class).contains(ChildClass.class));
        assertTrue(ReflectionUtils.getClassHierarchy(ChildInterface.class).contains(ChildInterface.class));
    }

    @Test
    void hierarchyContainsAllSuperclasses() {
        assertThat(ReflectionUtils.getClassHierarchy(int.class)).containsSubsequence(int.class);
        assertThat(ReflectionUtils.getClassHierarchy(Object.class)).containsSubsequence(Object.class);
        assertThat(ReflectionUtils.getClassHierarchy(ChildClass.class)).containsSubsequence(
            ChildClass.class,
            ParentClass.class,
            Object.class
        );
    }

    @Test
    void hierarchyContainsAllInterfaces() {
        assertThat(ReflectionUtils.getClassHierarchy(ChildClass.class)).containsSubsequence(
            ChildInterface.class,
            ParentInterface.class
        );
        assertThat(ReflectionUtils.getClassHierarchy(ChildClass.class)).containsSubsequence(
            TestInterface2.class,
            TestInterface1.class
        );
    }

    private interface TestInterface1 {
    }

    private interface TestInterface2 extends TestInterface1 {
    }

    private interface ParentInterface {
    }

    private interface ChildInterface extends ParentInterface {
    }

    private static class ParentClass implements ParentInterface {
    }

    private static class ChildClass extends ParentClass implements ChildInterface, TestInterface2 {
    }


    @Test
    void invokeDefaultMethod() throws Throwable {
        val method = DefaultMethodContainer.class.getMethod("getValue");
        val target = new DefaultMethodContainer() { };
        assertEquals("default", ReflectionUtils.invokeDefaultMethod(method, target));
    }

    private interface DefaultMethodContainer {
        @SuppressWarnings("UnusedMethod")
        default String getValue() {
            return "default";
        }
    }

}
