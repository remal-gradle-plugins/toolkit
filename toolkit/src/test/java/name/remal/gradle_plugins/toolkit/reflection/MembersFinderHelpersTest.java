package name.remal.gradle_plugins.toolkit.reflection;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MembersFinderHelpersTest {

    @SuppressWarnings("UnusedMethod")
    private static class StaticAndInstanceMethodWithoutParamsExample {
        public static void staticMethod() {
        }

        public void instanceMethod() {
        }
    }

    @Test
    void static_and_instance_methods_without_params() throws Throwable {
        Assertions.assertEquals(
            StaticAndInstanceMethodWithoutParamsExample.class.getMethod("staticMethod"),
            MembersFinderHelpers.findMethod(
                StaticAndInstanceMethodWithoutParamsExample.class,
                true,
                null,
                "staticMethod"
            )
        );
        assertNull(
            MembersFinderHelpers.findMethod(
                StaticAndInstanceMethodWithoutParamsExample.class,
                false,
                null,
                "staticMethod"
            )
        );

        assertNull(
            MembersFinderHelpers.findMethod(
                StaticAndInstanceMethodWithoutParamsExample.class,
                true,
                null,
                "instanceMethod"
            )
        );
        Assertions.assertEquals(
            StaticAndInstanceMethodWithoutParamsExample.class.getMethod("instanceMethod"),
            MembersFinderHelpers.findMethod(
                StaticAndInstanceMethodWithoutParamsExample.class,
                false,
                null,
                "instanceMethod"
            )
        );
    }

    @SuppressWarnings("UnusedMethod")
    private static class StaticAndInstanceMethodWithParamsExample {
        public static void staticMethod(int param) {
        }

        public void instanceMethod(int param) {
        }
    }

    @Test
    void static_and_instance_methods_with_params() throws Throwable {
        Assertions.assertEquals(
            StaticAndInstanceMethodWithParamsExample.class.getMethod("staticMethod", int.class),
            MembersFinderHelpers.findMethod(
                StaticAndInstanceMethodWithParamsExample.class,
                true,
                null,
                "staticMethod",
                int.class
            )
        );
        assertNull(
            MembersFinderHelpers.findMethod(
                StaticAndInstanceMethodWithParamsExample.class,
                false,
                null,
                "staticMethod",
                int.class
            )
        );

        assertNull(
            MembersFinderHelpers.findMethod(
                StaticAndInstanceMethodWithParamsExample.class,
                true,
                null,
                "instanceMethod",
                int.class
            )
        );
        Assertions.assertEquals(
            StaticAndInstanceMethodWithParamsExample.class.getMethod("instanceMethod", int.class),
            MembersFinderHelpers.findMethod(
                StaticAndInstanceMethodWithParamsExample.class,
                false,
                null,
                "instanceMethod",
                int.class
            )
        );
    }

    @Test
    void void_return_type_without_params() throws Throwable {
        @SuppressWarnings("UnusedMethod")
        class Example {
            public void method() {
            }
        }

        Assertions.assertEquals(
            Example.class.getMethod("method"),
            MembersFinderHelpers.findMethod(Example.class, false, null, "method")
        );
        Assertions.assertEquals(
            Example.class.getMethod("method"),
            MembersFinderHelpers.findMethod(Example.class, false, void.class, "method")
        );
        Assertions.assertEquals(
            Example.class.getMethod("method"),
            MembersFinderHelpers.findMethod(Example.class, false, Void.class, "method")
        );
        Assertions.assertEquals(
            Example.class.getMethod("method"),
            MembersFinderHelpers.findMethod(Example.class, false, Object.class, "method")
        );
        assertNull(
            MembersFinderHelpers.findMethod(Example.class, false, String.class, "method")
        );
    }

    @Test
    void void_return_type_with_params() throws Throwable {
        @SuppressWarnings("UnusedMethod")
        class Example {
            public void method(int param) {
            }
        }

        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, null, "method", int.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, void.class, "method", int.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, Void.class, "method", int.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, Object.class, "method", int.class)
        );
        assertNull(
            MembersFinderHelpers.findMethod(Example.class, false, String.class, "method", int.class)
        );
    }

    @Test
    void int_return_type_without_params() throws Throwable {
        @SuppressWarnings("UnusedMethod")
        class Example {
            public int method() {
                throw new UnsupportedOperationException();
            }
        }

        Assertions.assertEquals(
            Example.class.getMethod("method"),
            MembersFinderHelpers.findMethod(Example.class, false, null, "method")
        );
        Assertions.assertEquals(
            Example.class.getMethod("method"),
            MembersFinderHelpers.findMethod(Example.class, false, int.class, "method")
        );
        Assertions.assertEquals(
            Example.class.getMethod("method"),
            MembersFinderHelpers.findMethod(Example.class, false, Integer.class, "method")
        );
        Assertions.assertEquals(
            Example.class.getMethod("method"),
            MembersFinderHelpers.findMethod(Example.class, false, Number.class, "method")
        );
        Assertions.assertEquals(
            Example.class.getMethod("method"),
            MembersFinderHelpers.findMethod(Example.class, false, Object.class, "method")
        );

        assertNull(
            MembersFinderHelpers.findMethod(Example.class, false, String.class, "method")
        );
    }

    @Test
    void int_return_type_with_params() throws Throwable {
        @SuppressWarnings("UnusedMethod")
        class Example {
            public int method(int param) {
                throw new UnsupportedOperationException();
            }
        }

        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, null, "method", int.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, int.class, "method", int.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, Integer.class, "method", int.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, Number.class, "method", int.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, Object.class, "method", int.class)
        );

        assertNull(
            MembersFinderHelpers.findMethod(Example.class, false, String.class, "method", int.class)
        );
    }

    @Test
    void return_type_differentiate() throws Throwable {
        @SuppressWarnings("UnusedMethod")
        class Example {
            public int method(int param) {
                throw new UnsupportedOperationException();
            }

            public String method(String param) {
                throw new UnsupportedOperationException();
            }
        }

        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, int.class, "method", int.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", int.class),
            MembersFinderHelpers.findMethod(Example.class, false, Number.class, "method", int.class)
        );

        Assertions.assertEquals(
            Example.class.getMethod("method", String.class),
            MembersFinderHelpers.findMethod(Example.class, false, String.class, "method", String.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", String.class),
            MembersFinderHelpers.findMethod(Example.class, false, CharSequence.class, "method", String.class)
        );
    }

    @Test
    void params_compatibility() throws Throwable {
        @SuppressWarnings("UnusedMethod")
        class Example {
            public void method(List<?> param) {
            }
        }

        Assertions.assertEquals(
            Example.class.getMethod("method", List.class),
            MembersFinderHelpers.findMethod(Example.class, false, null, "method", List.class)
        );
        Assertions.assertEquals(
            Example.class.getMethod("method", List.class),
            MembersFinderHelpers.findMethod(Example.class, false, null, "method", ArrayList.class)
        );

        assertNull(
            MembersFinderHelpers.findMethod(Example.class, false, null, "method", Collection.class)
        );
    }

}
