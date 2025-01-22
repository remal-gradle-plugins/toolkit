package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

import com.google.auto.service.AutoService;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.internal.file.FileCollectionInternal.Source;
import org.gradle.api.internal.file.FileCollectionStructureVisitor;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.SubtractingFileCollection;
import org.gradle.api.internal.file.collections.FileSystemMirroringFileTree;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.util.PatternSet;
import org.jetbrains.annotations.Unmodifiable;

@ReliesOnInternalGradleApi
@AutoService(FileCollectionUtilsMethods.class)
final class FileCollectionUtilsMethods_7_9999_lt implements FileCollectionUtilsMethods {

    @Unmodifiable
    public Set<Configuration> getConfigurationsUsedIn(FileCollection rootFileCollection) {
        if (rootFileCollection instanceof Configuration) {
            return singleton((Configuration) rootFileCollection);
        }


        var seenConfigurations = new LinkedHashSet<Configuration>();

        var fileCollectionInternal = (FileCollectionInternal) rootFileCollection;
        fileCollectionInternal.visitStructure(new FileCollectionStructureVisitor() {
            @Override
            public boolean startVisit(Source source, FileCollectionInternal fileCollection) {
                if (fileCollection instanceof Configuration) {
                    seenConfigurations.add((Configuration) fileCollection);
                    return false;
                }

                if (fileCollection instanceof FileTree
                    || fileCollection instanceof SourceSetOutput
                ) {
                    return false;
                }

                if (fileCollection instanceof SubtractingFileCollection) {
                    ((SubtractingFileCollection) fileCollection).getLeft().visitStructure(this);
                    return false;
                }

                return true;
            }

            @Override
            public VisitType prepareForVisit(Source source) {
                return VisitType.Spec;
            }

            @Override
            public void visitCollection(Source source, Iterable<File> contents) {
                // do nothing
            }

            @Override
            public void visitFileTree(File root, PatternSet patterns, FileTreeInternal fileTree) {
                // do nothing
            }

            @Override
            public void visitGenericFileTree(FileTreeInternal fileTree, FileSystemMirroringFileTree sourceTree) {
                // do nothing
            }

            @Override
            public void visitFileTreeBackedByFile(
                File file,
                FileTreeInternal fileTree,
                FileSystemMirroringFileTree sourceTree
            ) {
                // do nothing
            }
        });

        return unmodifiableSet(seenConfigurations);
    }

}
