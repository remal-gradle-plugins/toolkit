package name.remal.gradleplugins.toolkit;

import static java.util.Collections.unmodifiableSet;

import com.google.auto.service.AutoService;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.val;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileCollectionInternal.Source;
import org.gradle.api.internal.file.FileCollectionStructureVisitor;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.tasks.util.PatternSet;
import org.jetbrains.annotations.Unmodifiable;

@AutoService(FileTreeUtilsMethods.class)
final class FileTreeUtilsMethods_6_2 extends FileTreeUtilsMethods {

    @Override
    @Unmodifiable
    @SuppressWarnings("java:S3776")
    public Set<File> getFileTreeRoots(FileTree fileTree) {
        Set<File> roots = new LinkedHashSet<>();

        val fileTreeInternal = (FileTreeInternal) fileTree;
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
                @Nullable FileTreeInternal fileTree
            ) {
                // there is no root here
            }

            @Override
            public void visitFileTree(
                @Nullable File root,
                @Nullable PatternSet patterns,
                @Nullable FileTreeInternal fileTree
            ) {
                if (root != null) {
                    roots.add(normalizeFile(root));
                }
            }

            @Override
            public void visitFileTreeBackedByFile(
                @Nullable File file,
                @Nullable FileTreeInternal fileTree
            ) {
                if (file != null) {
                    roots.add(normalizeFile(file));
                }
            }
        });

        return unmodifiableSet(roots);
    }

}
