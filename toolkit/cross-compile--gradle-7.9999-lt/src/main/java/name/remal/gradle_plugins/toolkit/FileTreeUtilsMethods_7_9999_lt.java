package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.unmodifiableSet;

import com.google.auto.service.AutoService;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileCollectionInternal.Source;
import org.gradle.api.internal.file.FileCollectionStructureVisitor;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.collections.DirectoryFileTree;
import org.gradle.api.internal.file.collections.FileSystemMirroringFileTree;
import org.gradle.api.tasks.util.PatternSet;
import org.jetbrains.annotations.Unmodifiable;

@ReliesOnInternalGradleApi
@AutoService(FileTreeUtilsMethods.class)
final class FileTreeUtilsMethods_7_9999_lt implements FileTreeUtilsMethods {

    @Override
    @Unmodifiable
    @SuppressWarnings("java:S3776")
    public Set<File> getFileTreeRoots(FileTree fileTree) {
        Set<File> roots = new LinkedHashSet<>();

        var fileTreeInternal = (FileTreeInternal) fileTree;
        fileTreeInternal.visitStructure(new FileCollectionStructureVisitor() {
            @Override
            public void visitCollection(
                @Nullable Source source,
                @Nullable Iterable<File> contents
            ) {
                // should not be called
            }

            @Override
            public void visitGenericFileTree(
                @Nullable FileTreeInternal fileTree,
                @Nullable FileSystemMirroringFileTree sourceTree
            ) {
                var dir = Optional.ofNullable(sourceTree)
                    .map(FileSystemMirroringFileTree::getMirror)
                    .map(DirectoryFileTree::getDir)
                    .orElse(null);
                if (dir != null) {
                    roots.add(normalizeFileTreeFile(dir));
                }
            }

            @Override
            public void visitFileTree(
                @Nullable File root,
                @Nullable PatternSet patterns,
                @Nullable FileTreeInternal fileTree
            ) {
                if (root != null) {
                    roots.add(normalizeFileTreeFile(root));
                }
            }

            @Override
            public void visitFileTreeBackedByFile(
                @Nullable File file,
                @Nullable FileTreeInternal fileTree,
                @Nullable FileSystemMirroringFileTree sourceTree
            ) {
                if (file != null) {
                    roots.add(normalizeFileTreeFile(file));
                }
            }
        });

        return unmodifiableSet(roots);
    }

}
