package name.remal.gradleplugins.toolkit.reflection;

import static name.remal.gradleplugins.toolkit.reflection.MembersFinderHelpers.findMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

class MembersFinderTest {

    private static class StaticAndInstanceMethodWithoutParamsExample {
        public static void staticMethod() {
        }

        public void instanceMethod() {
        }
    }

    @Test
    void static_and_instance_methods_without_params() throws Throwable {
        assertEquals(
            StaticAndInstanceMethodWithoutParamsExample.class.getMethod("staticMethod"),
            findMethod(StaticAndInstanceMethodWithoutParamsExample.class, true, null, "staticMethod")
        );
        assertNull(
            findMethod(StaticAndInstanceMethodWithoutParamsExample.class, false, null, "staticMethod")
        );

        assertNull(
            findMethod(StaticAndInstanceMethodWithoutParamsExample.class, true, null, "instanceMethod")
        );
        assertEquals(
            StaticAndInstanceMethodWithoutParamsExample.class.getMethod("instanceMethod"),
            findMethod(StaticAndInstanceMethodWithoutParamsExample.class, false, null, "instanceMethod")
        );
    }

    private static class StaticAndInstanceMethodWithParamsExample {
        public static void staticMethod(int param) {
        }

        public void instanceMethod(int param) {
        }
    }

    @Test
    void static_and_instance_methods_with_params() throws Throwable {
        assertEquals(
            StaticAndInstanceMethodWithParamsExample.class.getMethod("staticMethod", int.class),
            findMethod(StaticAndInstanceMethodWithParamsExample.class, true, null, "staticMethod", int.class)
        );
        assertNull(
            findMethod(StaticAndInstanceMethodWithParamsExample.class, false, null, "staticMethod", int.class)
        );

        assertNull(
            findMethod(StaticAndInstanceMethodWithParamsExample.class, true, null, "instanceMethod", int.class)
        );
        assertEquals(
            StaticAndInstanceMethodWithParamsExample.class.getMethod("instanceMethod", int.class),
            findMethod(StaticAndInstanceMethodWithParamsExample.class, false, null, "instanceMethod", int.class)
        );
    }

    @Test
    void void_return_type_without_params() throws Throwable {
        class Example {
            public void method() {
            }
        }

        assertEquals(
            Example.class.getMethod("method"),
            findMethod(Example.class, false, null, "method")
        );
        assertEquals(
            Example.class.getMethod("method"),
            findMethod(Example.class, false, void.class, "method")
        );
        assertEquals(
            Example.class.getMethod("method"),
            findMethod(Example.class, false, Void.class, "method")
        );
        assertEquals(
            Example.class.getMethod("method"),
            findMethod(Example.class, false, Object.class, "method")
        );
        assertNull(
            findMethod(Example.class, false, String.class, "method")
        );
    }

    @Test
    void void_return_type_with_params() throws Throwable {
        class Example {
            public void method(int param) {
            }
        }

        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, null, "method", int.class)
        );
        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, void.class, "method", int.class)
        );
        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, Void.class, "method", int.class)
        );
        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, Object.class, "method", int.class)
        );
        assertNull(
            findMethod(Example.class, false, String.class, "method", int.class)
        );
    }

    @Test
    void int_return_type_without_params() throws Throwable {
        class Example {
            public int method() {
                throw new UnsupportedOperationException();
            }
        }

        assertEquals(
            Example.class.getMethod("method"),
            findMethod(Example.class, false, null, "method")
        );
        assertEquals(
            Example.class.getMethod("method"),
            findMethod(Example.class, false, int.class, "method")
        );
        assertEquals(
            Example.class.getMethod("method"),
            findMethod(Example.class, false, Integer.class, "method")
        );
        assertEquals(
            Example.class.getMethod("method"),
            findMethod(Example.class, false, Number.class, "method")
        );
        assertEquals(
            Example.class.getMethod("method"),
            findMethod(Example.class, false, Object.class, "method")
        );

        assertNull(
            findMethod(Example.class, false, String.class, "method")
        );
    }

    @Test
    void int_return_type_with_params() throws Throwable {
        class Example {
            public int method(int param) {
                throw new UnsupportedOperationException();
            }
        }

        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, null, "method", int.class)
        );
        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, int.class, "method", int.class)
        );
        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, Integer.class, "method", int.class)
        );
        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, Number.class, "method", int.class)
        );
        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, Object.class, "method", int.class)
        );

        assertNull(
            findMethod(Example.class, false, String.class, "method", int.class)
        );
    }

    @Test
    void return_type_differentiate() throws Throwable {
        class Example {
            public int method(int param) {
                throw new UnsupportedOperationException();
            }

            public String method(String param) {
                throw new UnsupportedOperationException();
            }
        }

        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, int.class, "method", int.class)
        );
        assertEquals(
            Example.class.getMethod("method", int.class),
            findMethod(Example.class, false, Number.class, "method", int.class)
        );

        assertEquals(
            Example.class.getMethod("method", String.class),
            findMethod(Example.class, false, String.class, "method", String.class)
        );
        assertEquals(
            Example.class.getMethod("method", String.class),
            findMethod(Example.class, false, CharSequence.class, "method", String.class)
        );
    }

    @Test
    void params_compatibility() throws Throwable {
        class Example {
            public void method(List<?> param) {
            }
        }

        assertEquals(
            Example.class.getMethod("method", List.class),
            findMethod(Example.class, false, null, "method", List.class)
        );
        assertEquals(
            Example.class.getMethod("method", List.class),
            findMethod(Example.class, false, null, "method", ArrayList.class)
        );

        assertNull(
            findMethod(Example.class, false, null, "method", Collection.class)
        );
    }

}
