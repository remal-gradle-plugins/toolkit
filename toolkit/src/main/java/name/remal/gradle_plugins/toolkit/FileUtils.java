package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PathUtils.getPathLastModifiedIfExists;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import java.io.File;
import java.nio.file.attribute.FileTime;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class FileUtils {

    @SneakyThrows
    public static File normalizeFile(File file) {
        return normalizePath(file.toPath()).toFile();
    }

    @Nullable
    @SneakyThrows
    public static FileTime getFileLastModifiedIfExists(File file) {
        return getPathLastModifiedIfExists(file.toPath());
    }

}
