package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyProxy;

import java.io.File;
import java.util.Set;
import lombok.NoArgsConstructor;
import org.gradle.api.file.FileTree;

@NoArgsConstructor(access = PRIVATE)
public abstract class FileTreeUtils {

    private static final FileTreeUtilsMethods METHODS = asLazyProxy(
        FileTreeUtilsMethods.class,
        () -> loadCrossCompileService(FileTreeUtilsMethods.class)
    );


    public static Set<File> getFileTreeRoots(FileTree fileTree) {
        return METHODS.getFileTreeRoots(fileTree);
    }

}
