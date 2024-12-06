package name.remal.gradle_plugins.toolkit.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_MODULE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.jar.Manifest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.ModuleNode;

class ModuleNameParserTest {

    @Test
    void allParamsAreNull() {
        assertNull(ModuleNameParser.parseModuleName(
            null,
            null,
            (String) null
        ));
    }

    @Test
    void moduleInfo() {
        val classNode = new ClassNode();
        classNode.access = ACC_MODULE;
        classNode.name = "module-info";
        classNode.module = new ModuleNode("test.module", 0, null);

        val classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        classNode.accept(classWriter);
        val bytes = classWriter.toByteArray();

        assertEquals(
            classNode.module.name,
            ModuleNameParser.parseModuleName(
                () -> new ByteArrayInputStream(bytes),
                null,
                (String) null
            )
        );
    }

    @Test
    void manifest() throws Throwable {
        val manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("Automatic-Module-Name", "test.module");

        val out = new ByteArrayOutputStream();
        manifest.write(out);
        val bytes = out.toByteArray();

        assertEquals(
            "test.module",
            ModuleNameParser.parseModuleName(
                null,
                () -> new ByteArrayInputStream(bytes),
                (String) null
            )
        );
    }

    @Test
    void jarFilePath_simple() {
        assertEquals(
            "test.module",
            ModuleNameParser.parseModuleName(
                null,
                null,
                "/test-module-1.2.jar"
            )
        );
    }

    @Test
    void jarFilePath_uri() throws Throwable {
        assertEquals(
            "test.module",
            ModuleNameParser.parseModuleName(
                null,
                null,
                new URI("jar:file:/test-module-1.2.jar!/")
            )
        );
    }

    @Test
    void jarFilePath_url() throws Throwable {
        assertEquals(
            "test.module",
            ModuleNameParser.parseModuleName(
                null,
                null,
                new URL("https://example.com/test-module-1.2.jar?query#hash")
            )
        );
    }

}
