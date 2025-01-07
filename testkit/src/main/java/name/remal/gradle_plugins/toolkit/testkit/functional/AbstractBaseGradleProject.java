package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.unwrapProviders;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.PropertiesUtils.storeProperties;

import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleBuildFileContent;
import org.gradle.api.Action;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

@Getter
public abstract class AbstractBaseGradleProject<
    Block extends JavaLikeContent<Block>,
    BuildFileType extends GradleBuildFileContent<Block>
    > {

    @ForOverride
    protected abstract BuildFileType createBuildFileContent();

    @ForOverride
    protected abstract String getBuildFileName();


    private static final String GRADLE_PROPERTIES_RELATIVE_PATH = "gradle.properties";


    @SuppressWarnings("Slf4jLoggerShouldBePrivate")
    protected final Logger logger = Logging.getLogger(getClass());


    protected final File projectDir;
    protected final BuildFileType buildFile;
    protected final Map<String, Object> gradleProperties = new LinkedHashMap<>();

    AbstractBaseGradleProject(File projectDir) {
        this.projectDir = normalizeFile(projectDir);
        this.buildFile = createBuildFileContent();
    }

    public String getName() {
        return projectDir.getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    public void forBuildFile(Action<BuildFileType> action) {
        action.execute(buildFile);
    }

    public void forGradleProperties(Action<Map<String, Object>> action) {
        action.execute(gradleProperties);
    }

    public void setGradleProperties(Map<String, Object> gradleProperties) {
        this.gradleProperties.clear();
        this.gradleProperties.putAll(gradleProperties);
    }

    public void setGradleProperty(String key, @Nullable Object value) {
        gradleProperties.put(key, value);
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
        writeTextFile(getBuildFileName(), buildFile.toString());
        writeGradlePropertiesToDisk();
    }


    @SneakyThrows
    public void writeBinaryFile(String relativeFilePath, byte[] bytes) {
        if (GRADLE_PROPERTIES_RELATIVE_PATH.equals(relativeFilePath)) {
            throw new IllegalArgumentException(format(
                "Use methods of %s to set Gradle properties",
                this.getClass().getSimpleName()
            ));
        }

        val destPath = resolveRelativePath(relativeFilePath);
        createParentDirectories(destPath);
        write(destPath, bytes);
    }

    public void writeTextFile(String relativeFilePath, String content, Charset charset) {
        writeBinaryFile(relativeFilePath, content.getBytes(charset));
    }

    public void writeTextFile(String relativeFilePath, String content) {
        writeTextFile(relativeFilePath, content, UTF_8);
    }


    @SneakyThrows
    public final byte[] readBinaryFile(String relativeFilePath) {
        val destPath = resolveRelativePath(relativeFilePath);
        return readAllBytes(destPath);
    }

    public final String readTextFile(String relativeFilePath, Charset charset) {
        val bytes = readBinaryFile(relativeFilePath);
        return new String(bytes, charset);
    }

    public final String readTextFile(String relativeFilePath) {
        return readTextFile(relativeFilePath, UTF_8);
    }


    public Path resolveRelativePath(String relativeFilePath) {
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

        return destPath;
    }

}
