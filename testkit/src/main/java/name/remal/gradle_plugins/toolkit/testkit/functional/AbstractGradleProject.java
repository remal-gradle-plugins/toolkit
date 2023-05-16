package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.write;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Contract;

@Getter
abstract class AbstractGradleProject<Child extends AbstractGradleProject<Child>> {

    protected final File projectDir;
    protected final BuildFile buildFile;
    protected final Properties gradleProperties = new Properties();

    AbstractGradleProject(File projectDir) {
        this.projectDir = normalizeFile(projectDir.getAbsoluteFile());
        this.buildFile = new BuildFile(this.projectDir);
    }

    public final String getName() {
        return projectDir.getName();
    }

    @Override
    public final String toString() {
        return getName();
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    public final Child forBuildFile(Consumer<BuildFile> buildFileConsumer) {
        buildFileConsumer.accept(buildFile);
        return (Child) this;
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    public final synchronized Child forGradleProperties(Consumer<Properties> propertiesConsumer) {
        propertiesConsumer.accept(gradleProperties);
        return (Child) this;
    }

    @SneakyThrows
    protected final void writeGradlePropertiesToDisk() {
        val path = projectDir.toPath().resolve("gradle.properties");
        try (val outputStream = newOutputStream(createParentDirectories(path))) {
            gradleProperties.store(outputStream, null);
        }
    }

    @OverridingMethodsMustInvokeSuper
    protected void writeToDisk() {
        buildFile.writeToDisk();
        writeGradlePropertiesToDisk();
    }

    @Contract("_,_ -> this")
    @CanIgnoreReturnValue
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public final Child writeBinaryFile(String relativeFilePath, byte[] bytes) {
        val relativePath = Paths.get(relativeFilePath);
        if (relativePath.isAbsolute()) {
            throw new IllegalArgumentException("Not a relative path: " + relativeFilePath);
        }

        val projectPath = normalizePath(projectDir.toPath());
        val destPath = normalizePath(projectPath.resolve(relativePath));
        if (!destPath.startsWith(projectPath)) {
            throw new IllegalArgumentException(
                "Relative path refers to a file outside of the project dir: " + relativeFilePath
            );
        }

        createParentDirectories(destPath);
        write(destPath, bytes);

        return (Child) this;
    }

    @Contract("_,_,_ -> this")
    @CanIgnoreReturnValue
    public final Child writeTextFile(String relativeFilePath, String content, Charset charset) {
        return writeBinaryFile(relativeFilePath, content.getBytes(charset));
    }

    @Contract("_,_ -> this")
    @CanIgnoreReturnValue
    public final Child writeTextFile(String relativeFilePath, String content) {
        return writeTextFile(relativeFilePath, content, UTF_8);
    }

}
