package name.remal.gradleplugins.toolkit.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

}
