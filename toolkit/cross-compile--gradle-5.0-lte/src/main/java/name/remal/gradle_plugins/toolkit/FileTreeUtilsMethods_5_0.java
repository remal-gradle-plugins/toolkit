package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.unmodifiableSet;

import com.google.auto.service.AutoService;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.file.DirectoryTree;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.internal.file.FileCollectionLeafVisitor;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.collections.DirectoryFileTree;
import org.gradle.api.internal.file.collections.FileTreeAdapter;
import org.jetbrains.annotations.Unmodifiable;

@ReliesOnInternalGradleApi
@AutoService(FileTreeUtilsMethods.class)
final class FileTreeUtilsMethods_5_0 extends FileTreeUtilsMethods {

    @Override
    @Unmodifiable
    @SuppressWarnings("java:S3776")
    public Set<File> getFileTreeRoots(FileTree fileTree) {
        Set<File> roots = new LinkedHashSet<>();

        val fileTreeInternal = (FileTreeInternal) fileTree;
        fileTreeInternal.visitLeafCollections(new FileCollectionLeafVisitor() {
            @Override
            public void visitCollection(
                @Nullable FileCollectionInternal fileCollection
            ) {
                if (fileCollection != null) {
                    for (val file : fileCollection.getFiles()) {
                        if (file != null) {
                            roots.add(normalizeFile(file));
                        }
                    }
                }
            }

            @Override
            @SneakyThrows
            @SuppressWarnings("java:S3011")
            public void visitGenericFileTree(
                @Nullable FileTreeInternal fileTree
            ) {
                if (!(fileTree instanceof FileTreeAdapter)) {
                    return;
                }

                Object delegateTree = null;
                for (Class<?> fileTreeClass = fileTree.getClass();
                     fileTreeClass != Object.class;
                     fileTreeClass = fileTreeClass.getSuperclass()
                ) {
                    final Field treeField;
                    try {
                        treeField = fileTreeClass.getDeclaredField("tree");
                    } catch (NoSuchFieldException ignored) {
                        continue;
                    }

                    treeField.setAccessible(true);
                    delegateTree = treeField.get(fileTree);
                    break;
                }

                if (delegateTree == null) {
                    return;
                }

                for (Class<?> delegateTreeClass = delegateTree.getClass();
                     delegateTreeClass != Object.class;
                     delegateTreeClass = delegateTreeClass.getSuperclass()
                ) {
                    final Method getBackingFileMethod;
                    try {
                        getBackingFileMethod = delegateTreeClass.getDeclaredMethod("getBackingFile");
                    } catch (NoSuchMethodException ignored) {
                        continue;
                    }

                    getBackingFileMethod.setAccessible(true);
                    val backingFile = getBackingFileMethod.invoke(delegateTree);
                    if (backingFile instanceof File) {
                        roots.add(normalizeFile((File) backingFile));
                    }
                }
            }

            @Override
            public void visitDirectoryTree(
                @Nullable DirectoryFileTree fileTree
            ) {
                val dir = Optional.ofNullable(fileTree)
                    .map(DirectoryTree::getDir)
                    .orElse(null);
                if (dir != null) {
                    roots.add(normalizeFile(dir));
                }
            }
        });

        return unmodifiableSet(roots);
    }

}
