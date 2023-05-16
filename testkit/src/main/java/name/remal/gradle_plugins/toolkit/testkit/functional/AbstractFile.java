package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Objects.requireNonNull;

import java.io.File;
import lombok.SneakyThrows;

abstract class AbstractFile {

    public abstract String getContent();


    protected final File file;

    protected AbstractFile(File file) {
        this.file = file.getAbsoluteFile();
    }

    @SneakyThrows
    public final void writeToDisk() {
        createDirectories(requireNonNull(file.getParentFile()).toPath());
        write(file.toPath(), getContent().getBytes(UTF_8));
    }


    @Override
    public final String toString() {
        return file.toString();
    }

}
