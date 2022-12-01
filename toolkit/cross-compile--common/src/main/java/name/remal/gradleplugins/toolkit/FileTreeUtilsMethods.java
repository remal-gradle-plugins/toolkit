package name.remal.gradleplugins.toolkit;

import java.io.File;
import java.util.Set;
import lombok.SneakyThrows;
import org.gradle.api.file.FileTree;
import org.jetbrains.annotations.Unmodifiable;

abstract class FileTreeUtilsMethods {

    @Unmodifiable
    public abstract Set<File> getFileTreeRoots(FileTree fileTree);


    @SneakyThrows
    protected static File normalizeFile(File file) {
        return file.getAbsoluteFile().getCanonicalFile();
    }

}
