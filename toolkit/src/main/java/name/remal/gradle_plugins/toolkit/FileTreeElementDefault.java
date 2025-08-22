package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.getPosixFilePermissions;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.WillNotClose;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.file.FilePermissions;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.RelativePath;
import org.gradle.api.file.UserClassFilePermissions;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
class FileTreeElementDefault implements FileTreeElement {

    @Nullable
    private final File file;

    @Nullable
    private final RelativePath relativePath;


    @Override
    public File getFile() {
        if (file != null) {
            return file;
        }

        throw new IllegalStateException("File is not set");
    }

    @Override
    public boolean isDirectory() {
        if (relativePath != null) {
            return !relativePath.isFile();
        }

        if (file != null) {
            return file.isDirectory();
        }

        throw new IllegalStateException("File and relative path are not set");
    }

    @Override
    public long getLastModified() {
        if (file != null) {
            return file.lastModified();
        }

        throw new IllegalStateException("File is not set");
    }

    @Override
    public long getSize() {
        if (file != null) {
            return file.length();
        }

        throw new IllegalStateException("File is not set");
    }

    @Override
    @MustBeClosed
    @SneakyThrows
    public InputStream open() {
        if (file != null) {
            return newInputStream(file.toPath());
        }

        throw new IllegalStateException("File is not set");
    }

    @Override
    @SneakyThrows
    public void copyTo(@WillNotClose OutputStream output) {
        try (var in = open()) {
            in.transferTo(output);
        }
    }

    @Override
    @SneakyThrows
    public boolean copyTo(File target) {
        if (file != null) {
            if (isDirectory()) {
                createDirectories(target.toPath());

            } else {
                var parentFile = target.getParentFile();
                if (parentFile != null) {
                    createDirectories(parentFile.toPath());
                }
                try (var in = open()) {
                    try (var out = newOutputStream(target.toPath())) {
                        in.transferTo(out);
                    }
                }
            }

            setPosixFilePermissions(target.toPath(), getPosixFilePermissions(file.toPath()));

            return true;
        }

        throw new IllegalStateException("File is not set");
    }

    @Override
    public String getName() {
        if (relativePath != null) {
            return relativePath.getLastName();
        }

        if (file != null) {
            return file.getName();
        }

        throw new IllegalStateException("File and relative path are not set");
    }

    @Override
    public String getPath() {
        if (relativePath != null) {
            return relativePath.getPathString();
        }

        if (file != null) {
            return file.getPath();
        }

        throw new IllegalStateException("File and relative path are not set");
    }

    @Override
    public RelativePath getRelativePath() {
        if (relativePath != null) {
            return relativePath;
        }

        throw new IllegalStateException("Relative path is not set");
    }

    // keep for backward compatibility
    public int getMode() {
        return getPermissions().toUnixNumeric();
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public FilePermissions getPermissions() {
        if (file != null) {
            var permissions = getPosixFilePermissions(file.toPath());

            return new FilePermissions() {
                @Override
                public UserClassFilePermissions getUser() {
                    return new UserClassFilePermissions() {
                        @Override
                        public boolean getRead() {
                            return permissions.contains(OWNER_READ);
                        }

                        @Override
                        public boolean getWrite() {
                            return permissions.contains(OWNER_WRITE);
                        }

                        @Override
                        public boolean getExecute() {
                            return permissions.contains(OWNER_EXECUTE);
                        }
                    };
                }

                @Override
                public UserClassFilePermissions getGroup() {
                    return new UserClassFilePermissions() {
                        @Override
                        public boolean getRead() {
                            return permissions.contains(GROUP_READ);
                        }

                        @Override
                        public boolean getWrite() {
                            return permissions.contains(GROUP_WRITE);
                        }

                        @Override
                        public boolean getExecute() {
                            return permissions.contains(GROUP_EXECUTE);
                        }
                    };
                }

                @Override
                public UserClassFilePermissions getOther() {
                    return new UserClassFilePermissions() {
                        @Override
                        public boolean getRead() {
                            return permissions.contains(OTHERS_READ);
                        }

                        @Override
                        public boolean getWrite() {
                            return permissions.contains(OTHERS_WRITE);
                        }

                        @Override
                        public boolean getExecute() {
                            return permissions.contains(OTHERS_EXECUTE);
                        }
                    };
                }

                @Override
                @SuppressWarnings("OctalInteger")
                public int toUnixNumeric() {
                    return (permissions.contains(OWNER_READ) ? 0400 : 0)
                        | (permissions.contains(OWNER_WRITE) ? 0200 : 0)
                        | (permissions.contains(OWNER_EXECUTE) ? 0100 : 0)
                        | (permissions.contains(GROUP_READ) ? 0040 : 0)
                        | (permissions.contains(GROUP_WRITE) ? 0020 : 0)
                        | (permissions.contains(GROUP_EXECUTE) ? 0010 : 0)
                        | (permissions.contains(OTHERS_READ) ? 0004 : 0)
                        | (permissions.contains(OTHERS_WRITE) ? 0002 : 0)
                        | (permissions.contains(OTHERS_EXECUTE) ? 0001 : 0);
                }
            };
        }

        throw new IllegalStateException("File is not set");
    }

}
