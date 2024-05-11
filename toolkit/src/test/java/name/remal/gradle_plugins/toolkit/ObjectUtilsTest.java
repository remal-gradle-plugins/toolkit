package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ObjectUtils.unwrapProviders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ObjectUtilsTest {

    @Nested
    class UnwrapProviders {

        @Test
        @SuppressWarnings({"Guava", "java:S4738"})
        void guavaOptional() {
            assertNull(unwrapProviders(com.google.common.base.Optional.absent()));
            assertEquals("asd", unwrapProviders(com.google.common.base.Optional.of(
                new AtomicReference<>("asd")
            )));
        }

    }

}
