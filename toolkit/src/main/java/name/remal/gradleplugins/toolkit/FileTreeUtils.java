package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.CrossCompileServices.loadCrossCompileService;

import java.io.File;
import java.util.Set;
import lombok.NoArgsConstructor;
import org.gradle.api.file.FileTree;

@ReliesOnInternalGradleApi
@NoArgsConstructor(access = PRIVATE)
public abstract class FileTreeUtils {

    private static final FileTreeUtilsMethods METHODS =
        loadCrossCompileService(FileTreeUtilsMethods.class);


    public static Set<File> getFileTreeRoots(FileTree fileTree) {
        return METHODS.getFileTreeRoots(fileTree);
    }

}
