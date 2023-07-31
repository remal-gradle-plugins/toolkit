package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import name.remal.gradle_plugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PropertyUtilsTest {

    private final Property<String> property;

    PropertyUtilsTest(Project project) {
        property = project.getObjects().property(String.class);
    }

    @Test
    void finalizeValue() {
        assertDoesNotThrow(() -> PropertyUtils.finalizeValue(property));
    }

    @Test
    void finalizeValueOnRead() {
        assertDoesNotThrow(() -> PropertyUtils.finalizeValueOnRead(property));
    }

    @Test
    void disallowChanges() {
        assertDoesNotThrow(() -> PropertyUtils.disallowChanges(property));
    }

    @Test
    void disallowUnsafeRead() {
        assertDoesNotThrow(() -> PropertyUtils.disallowUnsafeRead(property));
    }


    @Nested
    @MinSupportedGradleVersion("5.6")
    class WithBasicSupport {

        @Test
        void getFinalized() {
            property.set("value");
            assertEquals("value", PropertyUtils.getFinalized(property));
            assertThrows(IllegalStateException.class, () -> property.set("other value"));
        }

        @Test
        void getOrNullFinalized() {
            assertNull(PropertyUtils.getOrNullFinalized(property));
            assertThrows(IllegalStateException.class, () -> property.set("value"));
        }

    }

}
