package name.remal.gradleplugins.toolkit;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.walkFileTree;
import static lombok.AccessLevel.PRIVATE;

import com.google.errorprone.annotations.CheckReturnValue;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class PathUtils {

    @SneakyThrows
    public static Path normalizePath(Path path) {
        final File file;
        try {
            file = path.toFile();
        } catch (UnsupportedOperationException ignored) {
            return path.toAbsolutePath().normalize();
        }

        return file.getCanonicalFile().toPath();
    }

    @Nullable
    @SneakyThrows
    public static FileTime getPathLastModifiedIfExists(Path path) {
        try {
            return getLastModifiedTime(path);
        } catch (NoSuchFileException ignored) {
            return null;
        }
    }

    private static final int DELETE_ATTEMPTS = 5;

    @SneakyThrows
    @SuppressWarnings({"BusyWait", "java:S1215"})
    public static Path deleteRecursively(Path path) {
        for (int attempt = 1; ; ++attempt) {
            try {
                walkFileTree(normalizePath(path), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        deleteIfExists(file);
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        deleteIfExists(dir);
                        return CONTINUE;
                    }
                });
                break;
            } catch (NoSuchFileException ignored) {
                break;
            } catch (FileSystemException e) {
                if (attempt >= DELETE_ATTEMPTS) {
                    throw e;
                } else {
                    // If we have some file descriptor leak, calling GC can help us, as it can execute finalizers
                    // which close file descriptors.
                    System.gc();
                    Thread.sleep(100L * attempt);
                }
            }
        }

        return path;
    }

    @CheckReturnValue
    @SneakyThrows
    @SuppressWarnings({"java:S1193", "ConstantConditions"})
    public static boolean tryToDeleteRecursively(Path path) {
        try {
            deleteRecursively(path);
            return true;
        } catch (Throwable e) {
            if (e instanceof FileSystemException) {
                return false;
            } else {
                throw e;
            }
        }
    }

    @SneakyThrows
    public static Path createParentDirectories(Path path) {
        val parentPath = normalizePath(path).getParent();
        if (parentPath != null) {
            createDirectories(parentPath);
        }
        return path;
    }

}
