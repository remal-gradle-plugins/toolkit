package name.remal.gradle_plugins.toolkit.reflection;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableList;
import static name.remal.gradle_plugins.toolkit.PredicateUtils.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WhoCalledUtilsTest {

    @Test
    void getCallingClasses() {
        var callingClassNames = WhoCalledUtils.getCallingClasses(1).stream()
            .map(Class::getName)
            .filter(not(name -> name.startsWith("jdk.internal.reflect.")))
            .filter(not(name -> name.startsWith("sun.reflect.")))
            .filter(not(name -> name.contains("$$Lambda$")))
            .filter(not("java.lang.reflect.Method"::equals))
            .collect(toUnmodifiableList());

        var expectedCallingClassNames = stream(new Exception().getStackTrace())
            .map(StackTraceElement::getClassName)
            .filter(not(name -> name.startsWith("jdk.internal.reflect.")))
            .filter(not(name -> name.startsWith("sun.reflect.")))
            .filter(not(name -> name.contains("$$Lambda$")))
            .filter(not("java.lang.reflect.Method"::equals))
            .toArray(String[]::new);

        assertThat(callingClassNames)
            .isNotEmpty()
            .contains(WhoCalledUtilsTest.class.getName())
            .containsExactly(expectedCallingClassNames);
    }

    @Test
    void getCallingClass() {
        assertEquals(WhoCalledUtilsTest.class, WhoCalledUtils.getCallingClass(1));
    }

    @Test
    void isCalledBy() {
        assertTrue(WhoCalledUtils.isCalledBy(WhoCalledUtilsTest.class));
    }

}
