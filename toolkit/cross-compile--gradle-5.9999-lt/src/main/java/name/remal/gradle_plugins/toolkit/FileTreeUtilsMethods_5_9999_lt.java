package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.unmodifiableSet;

import com.google.auto.service.AutoService;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.internal.file.FileCollectionLeafVisitor;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.tasks.util.PatternSet;
import org.jetbrains.annotations.Unmodifiable;

@ReliesOnInternalGradleApi
@AutoService(FileTreeUtilsMethods.class)
final class FileTreeUtilsMethods_5_9999_lt extends FileTreeUtilsMethods {

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
            public void visitGenericFileTree(
                @Nullable FileTreeInternal fileTree
            ) {
                // there is no root here
            }

            @Override
            public void visitFileTree(
                @Nullable File root,
                @Nullable PatternSet patterns
            ) {
                if (root != null) {
                    roots.add(normalizeFile(root));
                }
            }
        });

        return unmodifiableSet(roots);
    }

}
