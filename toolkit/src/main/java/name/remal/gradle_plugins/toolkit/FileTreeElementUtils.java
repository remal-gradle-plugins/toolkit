package name.remal.gradle_plugins.toolkit;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.BytecodeTestUtils.wrapWithTestClassVisitors;
import static name.remal.gradle_plugins.toolkit.InTestFlags.isInTest;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.defineClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.tryLoadClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.IXOR;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.RelativePath;
import org.jetbrains.annotations.VisibleForTesting;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

@NoArgsConstructor(access = PRIVATE)
public abstract class FileTreeElementUtils {

    @ReliesOnInternalGradleApi
    @VisibleForTesting
    static final Set<Class<?>> ABSTRACT_ARCHIVE_FILE_TREE_CLASSES = Stream.of(
            tryLoadClass("org.gradle.api.internal.file.archive.AbstractArchiveFileTree"),
            tryLoadClass("org.gradle.api.internal.file.collections.FileSystemMirroringFileTree")
        )
        .filter(Objects::nonNull)
        .collect(toCollection(LinkedHashSet::new));

    private static final Set<String> ARCHIVE_FILE_TREE_SIMPLE_CLASS_NAMES = unmodifiableSet(new LinkedHashSet<>(asList(
        // supported by Gradle natively:
        "TarFileTree",
        "ZipFileTree",

        // supported by https://github.com/freefair/gradle-plugins/:
        "ArFileTree",
        "ArchiveFileTree",
        "ArjFileTree",
        "DumpFileTree",
        "SevenZipFileTree"
    )));

    public static boolean isArchiveEntry(FileTreeElement details) {
        val detailsClass = unwrapGeneratedSubclass(details.getClass());
        val enclosingClass = detailsClass.getEnclosingClass();
        if (enclosingClass == null) {
            return false;
        }

        val isArchive = ABSTRACT_ARCHIVE_FILE_TREE_CLASSES.contains(enclosingClass)
            || ARCHIVE_FILE_TREE_SIMPLE_CLASS_NAMES.contains(enclosingClass.getSimpleName());
        return isArchive;
    }

    public static boolean isNotArchiveEntry(FileTreeElement details) {
        return !isArchiveEntry(details);
    }


    @SneakyThrows
    public static FileTreeElement mockedFileTreeElementFromRelativePath(RelativePath relativePath) {
        return MockedFileTreeElementFromRelativePathClassHolder.CLASS
            .getConstructor(RelativePath.class)
            .newInstance(relativePath);
    }

    private static class MockedFileTreeElementFromRelativePathClassHolder {

        public static final Class<FileTreeElement> CLASS = createClass();

        private static final boolean IN_TEST = isInTest();

        @SneakyThrows
        private static Class<FileTreeElement> createClass() {
            val classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
            ClassVisitor classVisitor = classWriter;
            if (IN_TEST) {
                classVisitor = wrapWithTestClassVisitors(classVisitor);
            }

            val classInternalName = getInternalName(FileTreeElement.class) + "$$MockForRelativePath";
            val classSuperName = FileTreeElement.class.isInterface()
                ? getInternalName(Object.class)
                : getInternalName(FileTreeElement.class);
            classVisitor.visit(
                V1_8,
                ACC_PUBLIC | ACC_SYNTHETIC,
                classInternalName,
                null,
                classSuperName,
                FileTreeElement.class.isInterface()
                    ? new String[]{getInternalName(FileTreeElement.class)}
                    : null
            );

            val delegateFieldName = "delegate";
            val delegateFieldDescr = getDescriptor(RelativePath.class);
            classVisitor.visitField(
                ACC_PRIVATE | ACC_FINAL,
                delegateFieldName,
                delegateFieldDescr,
                null,
                null
            );

            {
                val mv = classVisitor.visitMethod(
                    ACC_PUBLIC,
                    "<init>",
                    getMethodDescriptor(
                        VOID_TYPE,
                        getType(RelativePath.class)
                    ),
                    null,
                    null
                );
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(
                    INVOKESPECIAL,
                    classSuperName,
                    "<init>",
                    getMethodDescriptor(
                        VOID_TYPE
                    ),
                    false
                );

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(
                    PUTFIELD,
                    classInternalName,
                    delegateFieldName,
                    delegateFieldDescr
                );

                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            {
                val methodToImplement = FileTreeElement.class.getMethod("getRelativePath");
                val mv = classVisitor.visitMethod(
                    ACC_PUBLIC,
                    methodToImplement.getName(),
                    getMethodDescriptor(methodToImplement),
                    null,
                    null
                );
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(
                    GETFIELD,
                    classInternalName,
                    delegateFieldName,
                    delegateFieldDescr
                );

                mv.visitInsn(getType(methodToImplement.getReturnType()).getOpcode(IRETURN));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            {
                val methodToImplement = FileTreeElement.class.getMethod("getPath");
                val mv = classVisitor.visitMethod(
                    ACC_PUBLIC,
                    methodToImplement.getName(),
                    getMethodDescriptor(methodToImplement),
                    null,
                    null
                );
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(
                    GETFIELD,
                    classInternalName,
                    delegateFieldName,
                    delegateFieldDescr
                );

                val methodToDelegateTo = RelativePath.class.getMethod("getPathString");
                mv.visitMethodInsn(
                    methodToDelegateTo.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                    getInternalName(methodToDelegateTo.getDeclaringClass()),
                    methodToDelegateTo.getName(),
                    getMethodDescriptor(methodToDelegateTo),
                    methodToDelegateTo.getDeclaringClass().isInterface()
                );

                mv.visitInsn(getType(methodToImplement.getReturnType()).getOpcode(IRETURN));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            {
                val methodToImplement = FileTreeElement.class.getMethod("getName");
                val mv = classVisitor.visitMethod(
                    ACC_PUBLIC,
                    methodToImplement.getName(),
                    getMethodDescriptor(methodToImplement),
                    null,
                    null
                );
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(
                    GETFIELD,
                    classInternalName,
                    delegateFieldName,
                    delegateFieldDescr
                );

                val methodToDelegateTo = RelativePath.class.getMethod("getLastName");
                mv.visitMethodInsn(
                    methodToDelegateTo.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                    getInternalName(methodToDelegateTo.getDeclaringClass()),
                    methodToDelegateTo.getName(),
                    getMethodDescriptor(methodToDelegateTo),
                    methodToDelegateTo.getDeclaringClass().isInterface()
                );

                mv.visitInsn(getType(methodToImplement.getReturnType()).getOpcode(IRETURN));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            {
                val methodToImplement = FileTreeElement.class.getMethod("isDirectory");
                val mv = classVisitor.visitMethod(
                    ACC_PUBLIC,
                    methodToImplement.getName(),
                    getMethodDescriptor(methodToImplement),
                    null,
                    null
                );
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(
                    GETFIELD,
                    classInternalName,
                    delegateFieldName,
                    delegateFieldDescr
                );

                val methodToDelegateTo = RelativePath.class.getMethod("isFile");
                mv.visitMethodInsn(
                    methodToDelegateTo.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                    getInternalName(methodToDelegateTo.getDeclaringClass()),
                    methodToDelegateTo.getName(),
                    getMethodDescriptor(methodToDelegateTo),
                    methodToDelegateTo.getDeclaringClass().isInterface()
                );

                // negate current value:
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IXOR);

                mv.visitInsn(getType(methodToImplement.getReturnType()).getOpcode(IRETURN));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            {
                val methodToImplement = Object.class.getMethod("toString");
                val mv = classVisitor.visitMethod(
                    ACC_PUBLIC,
                    methodToImplement.getName(),
                    getMethodDescriptor(methodToImplement),
                    null,
                    null
                );
                mv.visitCode();

                mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(
                    INVOKESPECIAL,
                    "java/lang/StringBuilder",
                    "<init>",
                    "()V",
                    false
                );

                mv.visitLdcInsn("mock '");
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                    false
                );

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(
                    GETFIELD,
                    classInternalName,
                    delegateFieldName,
                    delegateFieldDescr
                );
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                    false
                );

                mv.visitLdcInsn("'");
                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                    false
                );

                mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "toString",
                    "()Ljava/lang/String;",
                    false
                );

                mv.visitInsn(getType(methodToImplement.getReturnType()).getOpcode(IRETURN));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            val bytecode = classWriter.toByteArray();
            val resultClass = defineClass(
                MockedFileTreeElementFromRelativePathClassHolder.class.getClassLoader(),
                bytecode
            );

            @SuppressWarnings("unchecked")
            val typedResultClass = (Class<FileTreeElement>) resultClass;
            return typedResultClass;
        }

    }

}
