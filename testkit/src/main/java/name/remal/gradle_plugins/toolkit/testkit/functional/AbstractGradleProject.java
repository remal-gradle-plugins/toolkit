package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static java.util.Collections.synchronizedMap;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.unwrapProviders;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.PropertiesUtils.storeProperties;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Contract;

@Getter
abstract class AbstractGradleProject<Child extends AbstractGradleProject<Child>> {

    private static final String GRADLE_PROPERTIES_RELATIVE_PATH = "gradle.properties";

    protected final File projectDir;
    protected final BuildFile buildFile;
    protected final Map<String, Object> gradleProperties = synchronizedMap(new LinkedHashMap<>());

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
    public final synchronized Child forGradleProperties(Consumer<Map<String, Object>> propertiesConsumer) {
        propertiesConsumer.accept(gradleProperties);
        return (Child) this;
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    public final synchronized Child setGradleProperties(Map<String, Object> gradleProperties) {
        this.gradleProperties.clear();
        this.gradleProperties.putAll(gradleProperties);
        return (Child) this;
    }

    @Contract("_,_ -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    public final synchronized Child setGradleProperty(String key, @Nullable Object value) {
        gradleProperties.put(key, value);
        return (Child) this;
    }

    @SneakyThrows
    protected final void writeGradlePropertiesToDisk() {
        val properties = new Properties();
        gradleProperties.forEach((key, value) -> {
            value = unwrapProviders(value);
            if (value != null) {
                properties.setProperty(key, value.toString());
            }
        });

        val path = projectDir.toPath().resolve(GRADLE_PROPERTIES_RELATIVE_PATH);
        storeProperties(properties, path);
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
        if (GRADLE_PROPERTIES_RELATIVE_PATH.equals(relativeFilePath)) {
            throw new IllegalArgumentException(format(
                "Use methods of %s to set Gradle properties",
                this.getClass().getSimpleName()
            ));
        }

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
