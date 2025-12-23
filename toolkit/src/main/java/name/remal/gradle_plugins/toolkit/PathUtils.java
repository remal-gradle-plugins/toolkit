package name.remal.gradle_plugins.toolkit;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.requireNonNullElseGet;
import static lombok.AccessLevel.PRIVATE;

import com.google.errorprone.annotations.CheckReturnValue;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class PathUtils {

    @SneakyThrows
    public static Path normalizePath(Path path) {
        path = path.toAbsolutePath().normalize();

        final File file;
        try {
            file = path.toFile();
        } catch (UnsupportedOperationException ignored) {
            return path;
        }

        return file.getCanonicalFile().toPath();
    }

    @SneakyThrows
    public static void copyRecursively(Path source, Path destination, CopyOption... options) {
        var withDefaultOptions = options.length == 0
            ? new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING}
            : options;

        var normalizedSource = normalizePath(source);
        var normalizedDestination = normalizePath(destination);
        walkFileTree(normalizedSource, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                var destinationPath = normalizedDestination.resolve(normalizedSource.relativize(dir).toString());
                createDirectories(destinationPath);
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                var destinationPath = normalizedDestination.resolve(normalizedSource.relativize(file).toString());
                copy(file, destinationPath, withDefaultOptions);
                return CONTINUE;
            }
        });
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
    @SuppressWarnings("java:S1215")
    public static Path deleteRecursively(Path path) {
        for (int attempt = 1; attempt <= DELETE_ATTEMPTS; ++attempt) {
            try {
                walkFileTree(normalizePath(path), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        deleteIfExists(file);
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exc) throws IOException {
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
                }

                Thread.sleep(100L * attempt);

                // If we have some file descriptor leaks, calling GC can help us, as it can execute finalizers
                // which close file descriptors.
                System.gc();
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

    public static void tryToDeleteRecursivelyIgnoringFailure(Path path) {
        if (!tryToDeleteRecursively(path)) {
            // ignore failure
        }
    }

    @SneakyThrows
    public static Path createParentDirectories(Path path) {
        var parentPath = normalizePath(path).getParent();
        if (parentPath != null) {
            createDirectories(parentPath);
        }
        return path;
    }


    private static final int MAX_LOCK_ATTEMPTS = 100;

    private static final Map<Path, PathLockEntry> PATH_LOCKS = new ConcurrentHashMap<>();

    private static final class PathLockEntry {
        final ReentrantLock lock = new ReentrantLock(true);
        final AtomicInteger refs = new AtomicInteger();
    }

    @Nullable
    @SneakyThrows
    @SuppressWarnings({"java:S1215", "java:S1143", "java:S3776"})
    public static <T> T withShortExclusiveLock(Path lockFilePath, FileExclusiveLockAction<T> action) {
        lockFilePath = normalizePath(lockFilePath);

        var packLockEntry = PATH_LOCKS.compute(lockFilePath, (__, entry) -> {
            entry = requireNonNullElseGet(entry, PathLockEntry::new);
            entry.refs.incrementAndGet();
            return entry;
        });
        packLockEntry.lock.lock();
        try {
            FileChannel channel = null;
            try {
                try {
                    channel = FileChannel.open(lockFilePath, CREATE, WRITE);
                } catch (NoSuchFileException ignored) {
                    createParentDirectories(lockFilePath);
                    channel = FileChannel.open(lockFilePath, CREATE, WRITE);
                }

                FileLock lock = null;
                try {
                    for (var attempt = 1; attempt <= MAX_LOCK_ATTEMPTS; attempt++) {
                        try {
                            lock = channel.tryLock(); // non-blocking
                            if (lock != null) {
                                break;
                            }
                        } catch (OverlappingFileLockException e) {
                            // Same-JVM overlap => retry
                        }

                        if (attempt >= MAX_LOCK_ATTEMPTS) {
                            throw new IllegalStateException("Failed to acquire lock on " + lockFilePath);
                        }

                        var sleepMillis = 50L * attempt;
                        Thread.sleep(sleepMillis);

                        // If we have some file channel leaks, calling GC can help us, as it can execute finalizers
                        // which close file channels.
                        System.gc();
                    }

                    return action.run();

                } finally {
                    if (lock != null && lock.isValid()) {
                        lock.release();
                    }
                }
            } finally {
                if (channel != null) {
                    channel.close();
                }
            }
        } finally {
            packLockEntry.lock.unlock();

            // cleanup to prevent map growth:
            PATH_LOCKS.computeIfPresent(lockFilePath, (__, entry) -> {
                if (entry != packLockEntry) { // defensive
                    return entry;
                }

                var refs = entry.refs.decrementAndGet();
                if (refs <= 0) {
                    return null;
                }

                return entry;
            });
        }
    }

    public static void withShortExclusiveLock(Path lockFilePath, FileExclusiveLockVoidAction action) {
        withShortExclusiveLock(lockFilePath, (FileExclusiveLockAction<Void>) () -> {
            action.run();
            return null;
        });
    }

    @FunctionalInterface
    public interface FileExclusiveLockAction<T> {
        @Nullable
        T run() throws Throwable;
    }

    @FunctionalInterface
    public interface FileExclusiveLockVoidAction {
        void run() throws Throwable;
    }

}
