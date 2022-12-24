package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.annotation.Nullable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class PropertiesConventionUtilsTest {

    private final Project project;

    @Data
    public static class TestExtension {
        @Nullable
        private String value;
    }

    @Test
    void setPropertyConvention() {
        val extension = project.getExtensions().create("testExt", TestExtension.class);
        assertNull(extension.getValue());

        PropertiesConventionUtils.setPropertyConvention(extension, "value", () -> "default");
        assertEquals("default", extension.getValue());

        extension.setValue("value");
        assertEquals("value", extension.getValue());
    }

    @Test
    void setPropertyConvention_unknown_property() {
        val extension = project.getExtensions().create("testExt", TestExtension.class);
        assertThrows(
            InvalidUserDataException.class,
            () -> PropertiesConventionUtils.setPropertyConvention(extension, "unknown", () -> "")
        );
    }

}
