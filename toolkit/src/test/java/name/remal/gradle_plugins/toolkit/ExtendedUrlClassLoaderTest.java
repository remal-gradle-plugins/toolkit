package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.newOutputStream;
import static java.util.Collections.list;
import static java.util.jar.Attributes.Name;
import static java.util.jar.Attributes.Name.MANIFEST_VERSION;
import static java.util.jar.JarFile.MANIFEST_NAME;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.ExtendedUrlClassLoader.LoadingOrder.PARENT_FIRST;
import static name.remal.gradle_plugins.toolkit.ExtendedUrlClassLoader.LoadingOrder.PARENT_ONLY;
import static name.remal.gradle_plugins.toolkit.ExtendedUrlClassLoader.LoadingOrder.SELF_FIRST;
import static name.remal.gradle_plugins.toolkit.ExtendedUrlClassLoader.LoadingOrder.SELF_ONLY;
import static name.remal.gradle_plugins.toolkit.PathUtils.tryToDeleteRecursively;
import static name.remal.gradle_plugins.toolkit.UrlUtils.openInputStreamForUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.V1_8;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

class ExtendedUrlClassLoaderTest {

    private static final String TEST_CLASS_NAME = "test.TestClass";
    private static final String TEST_CLASS_RESOURCE_NAME = TEST_CLASS_NAME.replace('.', '/') + ".class";

    private static final String TEST_PARENT_CLASS_NAME = "test.TestParentClass";
    private static final String TEST_PARENT_CLASS_RESOURCE_NAME = TEST_PARENT_CLASS_NAME.replace('.', '/') + ".class";

    private static final String TEST_CHILD_CLASS_NAME = "test.TestChildClass";
    private static final String TEST_CHILD_CLASS_RESOURCE_NAME = TEST_CHILD_CLASS_NAME.replace('.', '/') + ".class";


    private static Path tempDir;
    private static URL parentClassLoaderFileUrl;
    private static URL[] parentClassLoaderFileUrls;
    private static URL classLoaderFileUrl;
    private static URL[] classLoaderFileUrls;


    @Test
    void testParentFirstLoadClass() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(PARENT_FIRST, classLoaderFileUrls, parentClassLoader)) {
                assertSame(
                    String.class.getClassLoader(),
                    classLoader.loadClass(String.class.getName()).getClassLoader()
                );
                assertSame(parentClassLoader, classLoader.loadClass(TEST_CLASS_NAME).getClassLoader());
                assertSame(parentClassLoader, classLoader.loadClass(TEST_PARENT_CLASS_NAME).getClassLoader());
                assertSame(classLoader, classLoader.loadClass(TEST_CHILD_CLASS_NAME).getClassLoader());
            }
        }
    }

    @Test
    void testSelfFirstLoadClass() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(SELF_FIRST, classLoaderFileUrls, parentClassLoader)) {
                assertSame(
                    String.class.getClassLoader(),
                    classLoader.loadClass(String.class.getName()).getClassLoader()
                );
                assertSame(classLoader, classLoader.loadClass(TEST_CLASS_NAME).getClassLoader());
                assertSame(parentClassLoader, classLoader.loadClass(TEST_PARENT_CLASS_NAME).getClassLoader());
                assertSame(classLoader, classLoader.loadClass(TEST_CHILD_CLASS_NAME).getClassLoader());
            }
        }
    }

    @Test
    void testParentOnlyLoadClass() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(PARENT_ONLY, classLoaderFileUrls, parentClassLoader)) {
                assertSame(
                    String.class.getClassLoader(),
                    classLoader.loadClass(String.class.getName()).getClassLoader()
                );

                assertSame(parentClassLoader, classLoader.loadClass(TEST_CLASS_NAME).getClassLoader());

                assertSame(parentClassLoader, classLoader.loadClass(TEST_PARENT_CLASS_NAME).getClassLoader());

                try {
                    assertSame(classLoader, classLoader.loadClass(TEST_CHILD_CLASS_NAME).getClassLoader());
                    fail();
                } catch (ClassNotFoundException ignored) {
                    // OK
                }
            }
        }
    }

    @Test
    void testSelfOnlyLoadClass() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(SELF_ONLY, classLoaderFileUrls, parentClassLoader)) {
                assertSame(
                    String.class.getClassLoader(),
                    classLoader.loadClass(String.class.getName()).getClassLoader()
                );

                assertSame(classLoader, classLoader.loadClass(TEST_CLASS_NAME).getClassLoader());

                try {
                    assertSame(parentClassLoader, classLoader.loadClass(TEST_PARENT_CLASS_NAME).getClassLoader());
                    fail();
                } catch (ClassNotFoundException ignored) {
                    // OK
                }

                assertSame(classLoader, classLoader.loadClass(TEST_CHILD_CLASS_NAME).getClassLoader());
            }
        }
    }


    @Test
    void testParentFirstGetResource() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(PARENT_FIRST, classLoaderFileUrls, parentClassLoader)) {
                assertThat(classLoader.getResource(TEST_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + parentClassLoaderFileUrl + "!");
                assertThat(classLoader.getResource(TEST_PARENT_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + parentClassLoaderFileUrl + "!");
                assertThat(classLoader.getResource(TEST_CHILD_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + classLoaderFileUrl + "!");
            }
        }
    }

    @Test
    void testSelfFirstGetResource() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(SELF_FIRST, classLoaderFileUrls, parentClassLoader)) {
                assertThat(classLoader.getResource(TEST_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + classLoaderFileUrl + "!");
                assertThat(classLoader.getResource(TEST_PARENT_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + parentClassLoaderFileUrl + "!");
                assertThat(classLoader.getResource(TEST_CHILD_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + classLoaderFileUrl + "!");
            }
        }
    }

    @Test
    void testParentOnlyGetResource() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(PARENT_ONLY, classLoaderFileUrls, parentClassLoader)) {
                assertThat(classLoader.getResource(TEST_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + parentClassLoaderFileUrl + "!");
                assertThat(classLoader.getResource(TEST_PARENT_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + parentClassLoaderFileUrl + "!");
                assertThat(classLoader.getResource(TEST_CHILD_CLASS_RESOURCE_NAME))
                    .isNull();
            }
        }
    }

    @Test
    void testSelfOnlyGetResource() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(SELF_ONLY, classLoaderFileUrls, parentClassLoader)) {
                assertThat(classLoader.getResource(TEST_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + classLoaderFileUrl + "!");
                assertThat(classLoader.getResource(TEST_PARENT_CLASS_RESOURCE_NAME))
                    .isNull();
                assertThat(classLoader.getResource(TEST_CHILD_CLASS_RESOURCE_NAME))
                    .isNotNull()
                    .asString()
                    .startsWith("jar:" + classLoaderFileUrl + "!");
            }
        }
    }


    @Test
    void testParentFirstGetResources() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(PARENT_FIRST, classLoaderFileUrls, parentClassLoader)) {
                val manifests = list(classLoader.getResources(MANIFEST_NAME)).stream()
                    .map(ExtendedUrlClassLoaderTest::readManifest)
                    .collect(toList());
                assertThat(manifests)
                    .hasSizeGreaterThan(2);
                assertEquals(
                    manifests.size() - 2,
                    indexOf(
                        manifests,
                        it -> "parent".equals(it.getMainAttributes()
                            .getValue(ExtendedUrlClassLoaderTest.class.getSimpleName()))
                    )
                );
                assertEquals(
                    manifests.size() - 1,
                    indexOf(
                        manifests,
                        it -> "child".equals(it.getMainAttributes()
                            .getValue(ExtendedUrlClassLoaderTest.class.getSimpleName()))
                    )
                );
            }
        }
    }

    @Test
    void testSelfFirstGetResources() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(SELF_FIRST, classLoaderFileUrls, parentClassLoader)) {
                val manifests = list(classLoader.getResources(MANIFEST_NAME)).stream()
                    .map(ExtendedUrlClassLoaderTest::readManifest)
                    .collect(toList());
                assertThat(manifests)
                    .hasSizeGreaterThan(2);
                assertEquals(
                    manifests.size() - 1,
                    indexOf(
                        manifests,
                        it -> "parent".equals(it.getMainAttributes()
                            .getValue(ExtendedUrlClassLoaderTest.class.getSimpleName()))
                    )
                );
                assertEquals(
                    0,
                    indexOf(
                        manifests,
                        it -> "child".equals(it.getMainAttributes()
                            .getValue(ExtendedUrlClassLoaderTest.class.getSimpleName()))
                    )
                );
            }
        }
    }

    @Test
    void testParentOnlyGetResources() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(PARENT_ONLY, classLoaderFileUrls, parentClassLoader)) {
                val manifests = list(classLoader.getResources(MANIFEST_NAME)).stream()
                    .map(ExtendedUrlClassLoaderTest::readManifest)
                    .collect(toList());
                assertThat(manifests)
                    .hasSizeGreaterThan(1);
                assertEquals(
                    manifests.size() - 1,
                    indexOf(
                        manifests,
                        it -> "parent".equals(it.getMainAttributes()
                            .getValue(ExtendedUrlClassLoaderTest.class.getSimpleName()))
                    )
                );
                assertEquals(
                    -1,
                    indexOf(
                        manifests,
                        it -> "child".equals(it.getMainAttributes()
                            .getValue(ExtendedUrlClassLoaderTest.class.getSimpleName()))
                    )
                );
            }
        }
    }

    @Test
    void testSelfOnlyGetResources() throws Exception {
        try (val parentClassLoader = new URLClassLoader(parentClassLoaderFileUrls)) {
            try (val classLoader = new ExtendedUrlClassLoader(SELF_ONLY, classLoaderFileUrls, parentClassLoader)) {
                val manifests = list(classLoader.getResources(MANIFEST_NAME)).stream()
                    .map(ExtendedUrlClassLoaderTest::readManifest)
                    .collect(toList());
                assertThat(manifests)
                    .hasSize(1);
                assertEquals(
                    -1,
                    indexOf(
                        manifests,
                        it -> "parent".equals(it.getMainAttributes()
                            .getValue(ExtendedUrlClassLoaderTest.class.getSimpleName()))
                    )
                );
                assertEquals(
                    0,
                    indexOf(
                        manifests,
                        it -> "child".equals(it.getMainAttributes()
                            .getValue(ExtendedUrlClassLoaderTest.class.getSimpleName()))
                    )
                );
            }
        }
    }


    @BeforeAll
    static void beforeAll() throws Throwable {
        tempDir = createTempDirectory(ExtendedUrlClassLoaderTest.class.getSimpleName() + '-');

        val parentClassLoaderFile = tempDir.resolve("parent.jar");
        try (val fileOutputStream = newOutputStream(parentClassLoaderFile)) {
            parentClassLoaderFileUrls = new URL[]{parentClassLoaderFileUrl = parentClassLoaderFile.toUri().toURL()};
            val manifest = new Manifest();
            manifest.getMainAttributes().put(MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(new Name(ExtendedUrlClassLoaderTest.class.getSimpleName()), "parent");
            try (val jarOutputStream = new JarOutputStream(fileOutputStream, manifest)) {
                jarOutputStream.putNextEntry(new JarEntry(TEST_CLASS_RESOURCE_NAME));
                jarOutputStream.write(generateBytecode(TEST_CLASS_NAME));

                jarOutputStream.putNextEntry(new JarEntry(TEST_PARENT_CLASS_RESOURCE_NAME));
                jarOutputStream.write(generateBytecode(TEST_PARENT_CLASS_NAME));
            }
        }

        val classLoaderFile = tempDir.resolve("child.jar");
        try (val fileOutputStream = newOutputStream(classLoaderFile)) {
            classLoaderFileUrls = new URL[]{classLoaderFileUrl = classLoaderFile.toUri().toURL()};
            val manifest = new Manifest();
            manifest.getMainAttributes().put(MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(new Name(ExtendedUrlClassLoaderTest.class.getSimpleName()), "child");
            try (val jarOutputStream = new JarOutputStream(fileOutputStream, manifest)) {
                jarOutputStream.putNextEntry(new JarEntry(TEST_CLASS_RESOURCE_NAME));
                jarOutputStream.write(generateBytecode(TEST_CLASS_NAME));

                jarOutputStream.putNextEntry(new JarEntry(TEST_CHILD_CLASS_RESOURCE_NAME));
                jarOutputStream.write(generateBytecode(TEST_CHILD_CLASS_NAME));
            }
        }
    }

    @AfterAll
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void afterAll() {
        tryToDeleteRecursively(tempDir);
    }

    private static byte[] generateBytecode(String className) {
        val classNode = new ClassNode();
        classNode.version = V1_8;
        classNode.access = ACC_ABSTRACT | ACC_INTERFACE;
        classNode.name = className.replace('.', '/');
        classNode.superName = "java/lang/Object";

        val classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    @SneakyThrows
    private static Manifest readManifest(URL url) {
        try (val inputStream = openInputStreamForUrl(url)) {
            return new Manifest(inputStream);
        }
    }

    private static <T> int indexOf(List<T> list, Predicate<T> predicate) {
        for (int i = 0; i < list.size(); ++i) {
            if (predicate.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

}
