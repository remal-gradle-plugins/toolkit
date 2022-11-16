package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.PathUtils.getPathLastModifiedIfExists;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;

import java.io.File;
import java.nio.file.attribute.FileTime;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = PRIVATE)
public abstract class FileUtils {

    public static File normalizeFile(File file) {
        return normalizePath(file.toPath()).toFile();
    }

    @Nullable
    @SneakyThrows
    public static FileTime getFileLastModifiedIfExists(File file) {
        return getPathLastModifiedIfExists(file.toPath());
    }

}
