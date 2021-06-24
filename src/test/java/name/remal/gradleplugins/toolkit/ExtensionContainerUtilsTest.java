package name.remal.gradleplugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.plugins.ExtensionAware;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ExtensionContainerUtilsTest {

    private final Project project;


    public interface TestExtension {
        String getValue();
    }

    @RequiredArgsConstructor
    @Getter
    @ToString
    @SuppressWarnings("MissingOverride")
    public static class TestExtensionImpl implements TestExtension {
        private final String value;
    }


    @Test
    void getExtensions() {
        assertSame(
            project.getExtensions(),
            ExtensionContainerUtils.getExtensions(project)
        );

        val testExtension = project.getExtensions().create("testExt", TestExtensionImpl.class, "");
        assertSame(
            ((ExtensionAware) testExtension).getExtensions(),
            ExtensionContainerUtils.getExtensions(testExtension)
        );
    }


    @Test
    void createExtension_publicType_name() {
        ExtensionContainerUtils.createExtension(
            project,
            TestExtension.class,
            "testExt",
            TestExtensionImpl.class,
            "value"
        );
        val extension = project.getExtensions().getByName("testExt");
        assertTrue(extension instanceof TestExtensionImpl);
        assertEquals("value", ((TestExtension) extension).getValue());
    }

    @Test
    void createExtension_name() {
        ExtensionContainerUtils.createExtension(
            project,
            "testExt",
            TestExtensionImpl.class,
            "value"
        );
        val extension = project.getExtensions().getByName("testExt");
        assertTrue(extension instanceof TestExtensionImpl);
        assertEquals("value", ((TestExtension) extension).getValue());
    }

    @Test
    void createExtension_publicType() {
        ExtensionContainerUtils.createExtension(
            project,
            TestExtension.class,
            TestExtensionImpl.class,
            "value"
        );
        val extension = project.getExtensions().getByName("testExtension");
        assertTrue(extension instanceof TestExtensionImpl);
        assertEquals("value", ((TestExtension) extension).getValue());
    }

    @Test
    void createExtension() {
        ExtensionContainerUtils.createExtension(
            project,
            TestExtensionImpl.class,
            "value"
        );
        val extension = project.getExtensions().getByName("testExtensionImpl");
        assertTrue(extension instanceof TestExtensionImpl);
        assertEquals("value", ((TestExtension) extension).getValue());
    }


    @Test
    void addExtension_publicType_name() {
        val extension = new TestExtensionImpl("");
        ExtensionContainerUtils.addExtension(project, TestExtension.class, "testExt", extension);
        assertTrue(project.getExtensions().getByName("testExt") instanceof TestExtensionImpl);
    }

    @Test
    void addExtension_name() {
        val extension = new TestExtensionImpl("");
        ExtensionContainerUtils.addExtension(project, "testExt", extension);
        assertTrue(project.getExtensions().getByName("testExt") instanceof TestExtensionImpl);
    }

    @Test
    void addExtension_publicType() {
        val extension = new TestExtensionImpl("");
        ExtensionContainerUtils.addExtension(project, TestExtension.class, extension);
        assertTrue(project.getExtensions().getByName("testExtension") instanceof TestExtensionImpl);
    }

    @Test
    void addExtension() {
        val extension = new TestExtensionImpl("");
        ExtensionContainerUtils.addExtension(project, extension);
        assertTrue(project.getExtensions().getByName("testExtensionImpl") instanceof TestExtensionImpl);
    }


    @Test
    void hasExtension_name() {
        assertFalse(ExtensionContainerUtils.hasExtension(project, "testExt"));
        project.getExtensions().create("testExt", TestExtensionImpl.class, "");
        assertTrue(ExtensionContainerUtils.hasExtension(project, "testExt"));
    }

    @Test
    void hasExtension_type() {
        assertFalse(ExtensionContainerUtils.hasExtension(project, TestExtension.class));
        assertFalse(ExtensionContainerUtils.hasExtension(project, TestExtensionImpl.class));
        project.getExtensions().create("testExt", TestExtensionImpl.class, "");
        assertTrue(ExtensionContainerUtils.hasExtension(project, TestExtension.class));
        assertTrue(ExtensionContainerUtils.hasExtension(project, TestExtensionImpl.class));
    }


    @Test
    void findExtension_name() {
        assertNull(ExtensionContainerUtils.findExtension(project, "testExt"));
        val extension = project.getExtensions().create("testExt", TestExtensionImpl.class, "");
        assertSame(extension, ExtensionContainerUtils.findExtension(project, "testExt"));
    }

    @Test
    void findExtension_type() {
        assertNull(ExtensionContainerUtils.findExtension(project, TestExtension.class));
        assertNull(ExtensionContainerUtils.findExtension(project, TestExtensionImpl.class));
        val extension = project.getExtensions().create("testExt", TestExtensionImpl.class, "");
        assertSame(extension, ExtensionContainerUtils.findExtension(project, TestExtension.class));
        assertSame(extension, ExtensionContainerUtils.findExtension(project, TestExtensionImpl.class));
    }


    @Test
    void getExtension_name() {
        assertThrows(
            UnknownDomainObjectException.class,
            () -> ExtensionContainerUtils.getExtension(project, "testExt")
        );
        val extension = project.getExtensions().create("testExt", TestExtensionImpl.class, "");
        assertSame(extension, ExtensionContainerUtils.getExtension(project, "testExt"));
    }

    @Test
    void getExtension_type() {
        assertThrows(
            UnknownDomainObjectException.class,
            () -> ExtensionContainerUtils.getExtension(project, TestExtension.class)
        );
        assertThrows(
            UnknownDomainObjectException.class,
            () -> ExtensionContainerUtils.getExtension(project, TestExtensionImpl.class)
        );
        val extension = project.getExtensions().create("testExt", TestExtensionImpl.class, "");
        assertSame(extension, ExtensionContainerUtils.getExtension(project, TestExtension.class));
        assertSame(extension, ExtensionContainerUtils.getExtension(project, TestExtensionImpl.class));
    }

}
