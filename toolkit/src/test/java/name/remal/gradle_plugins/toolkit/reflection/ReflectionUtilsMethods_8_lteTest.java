package name.remal.gradle_plugins.toolkit.reflection;

import static name.remal.gradle_plugins.toolkit.ArchiveUtils.newZipArchiveWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Type.getInternalName;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

@SuppressWarnings("NewClassNamingConvention")
class ReflectionUtilsMethods_8_lteTest {

    final ReflectionUtilsMethods_8_lte methods = new ReflectionUtilsMethods_8_lte();

    @Test
    void moduleNameOf_file_name(@TempDir Path tempDir) throws Throwable {
        val jarFilePath = tempDir.resolve("dependency-1.2.jar");
        val className = "test_package.Test" + System.nanoTime();
        try (val writer = newZipArchiveWriter(jarFilePath)) {
            val classNode = new ClassNode();
            classNode.version = V1_8;
            classNode.name = className.replace('.', '/');
            classNode.superName = getInternalName(Object.class);

            val classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
            classNode.accept(classWriter);
            val bytes = classWriter.toByteArray();

            writer.writeEntry(classNode.name + ".class", bytes);
        }

        val jarUrl = new URL("jar:" + jarFilePath.toUri() + "!/");

        final Class<?> clazz;
        try (val classLoader = new URLClassLoader(new URL[]{jarUrl})) {
            clazz = classLoader.loadClass(className);
        }

        assertEquals(
            "dependency",
            methods.moduleNameOf(clazz)
        );
    }

}
