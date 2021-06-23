package name.remal.gradleplugins.toolkit.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

class ClassUtilsTest {

    @Test
    void tryLoadClass() {
        assertEquals(
            ClassUtilsTest.class,
            ClassUtils.tryLoadClass(
                ClassUtilsTest.class.getName(),
                ClassUtilsTest.class.getClassLoader()
            )
        );
        assertEquals(
            ClassUtilsTest.class,
            ClassUtils.tryLoadClass(
                ClassUtilsTest.class.getName()
            )
        );

        assertNull(
            ClassUtils.tryLoadClass(
                ClassUtilsTest.class.getName() + ":wrong-class-name-suffix",
                ClassUtilsTest.class.getClassLoader()
            )
        );
        assertNull(
            ClassUtils.tryLoadClass(
                ClassUtilsTest.class.getName() + ":wrong-class-name-suffix"
            )
        );
    }

    @Test
    void isClassPresent() {
        assertTrue(
            ClassUtils.isClassPresent(
                ClassUtilsTest.class.getName(),
                ClassUtilsTest.class.getClassLoader()
            )
        );
        assertTrue(
            ClassUtils.isClassPresent(
                ClassUtilsTest.class.getName()
            )
        );

        assertFalse(
            ClassUtils.isClassPresent(
                ClassUtilsTest.class.getName() + ":wrong-class-name-suffix",
                ClassUtilsTest.class.getClassLoader()
            )
        );
        assertFalse(
            ClassUtils.isClassPresent(
                ClassUtilsTest.class.getName() + ":wrong-class-name-suffix"
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
        val unwrappedGeneratedSubclass = ClassUtils.unwrapGeneratedSubclass(taskType);
        assertEquals(TestTask.class, unwrappedGeneratedSubclass);
    }

}
