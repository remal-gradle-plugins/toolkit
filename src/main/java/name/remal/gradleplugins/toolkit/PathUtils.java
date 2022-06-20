package name.remal.gradleplugins.toolkit;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.walkFileTree;
import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public abstract class PathUtils {

    public static Path normalizePath(Path path) {
        return path.toAbsolutePath().normalize();
    }

    @SneakyThrows
    public static Path deleteRecursively(Path path) {
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
        } catch (NoSuchFileException ignored) {
            // do nothing
        }

        return path;
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
