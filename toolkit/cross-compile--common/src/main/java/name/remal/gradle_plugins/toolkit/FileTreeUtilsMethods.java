package name.remal.gradle_plugins.toolkit;

import java.io.File;
import java.util.Set;
import lombok.SneakyThrows;
import org.gradle.api.file.FileTree;
import org.jetbrains.annotations.Unmodifiable;

interface FileTreeUtilsMethods {

    @Unmodifiable
    Set<File> getFileTreeRoots(FileTree fileTree);


    @SneakyThrows
    default File normalizeFileTreeFile(File file) {
        return file.getAbsoluteFile().getCanonicalFile();
    }

}
