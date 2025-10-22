package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.util.function.Predicate.not;
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
import java.util.Optional;
import java.util.Properties;
import lombok.Getter;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.generate_sources.generators.TextContent;
import name.remal.gradle_plugins.generate_sources.generators.TextContentDefault;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.generate_sources.generators.java_like.java.JavaClassFileContent;
import name.remal.gradle_plugins.generate_sources.generators.java_like.java.JavaClassFileContentDefault;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleBuildFileContent;
import org.gradle.api.Action;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jspecify.annotations.Nullable;

@Getter
public abstract class AbstractBaseGradleProject<
    Block extends JavaLikeContent<Block>,
    BuildFileType extends GradleBuildFileContent<Block>
    > {

    private static final Charset DEFAULT_TEST_FILE_CHARSET = UTF_8;


    @ForOverride
    protected abstract BuildFileType createBuildFileContent();

    @ForOverride
    protected abstract String getBuildFileName();


    private static final String GRADLE_PROPERTIES_RELATIVE_PATH = "gradle.properties";


    @SuppressWarnings("Slf4jLoggerShouldBePrivate")
    protected final Logger logger = Logging.getLogger(getClass());


    protected final File projectDir;
    protected final BuildFileType buildFile;
    protected final Map<String, @Nullable Object> gradleProperties = new LinkedHashMap<>();

    AbstractBaseGradleProject(File projectDir) {
        this.projectDir = normalizeFile(projectDir);
        this.buildFile = createBuildFileContent();
    }

    public final String getName() {
        return projectDir.getName();
    }

    @Override
    public final String toString() {
        return getName();
    }

    public final void forBuildFile(Action<? super BuildFileType> action) {
        action.execute(buildFile);
    }

    public final void forGradleProperties(Action<? super Map<String, @Nullable Object>> action) {
        action.execute(gradleProperties);
    }

    public final void cleanGradleProperties() {
        this.gradleProperties.clear();
    }

    @SuppressWarnings("java:S2259")
    public final void putGradleProperties(Map<String, @Nullable Object> gradleProperties) {
        gradleProperties.forEach(this::putGradleProperty);
    }

    public final void putGradleProperty(String key, @Nullable Object value) {
        if (value != null) {
            gradleProperties.put(key, value);
        } else {
            gradleProperties.remove(key);
        }
    }

    public final void putGradlePropertyIfAbsent(String key, @Nullable Object value) {
        if (value != null && gradleProperties.get(key) == null) {
            gradleProperties.put(key, value);
        }
    }

    @Nullable
    public final Object getGradleProperty(String key) {
        return gradleProperties.get(key);
    }

    @SneakyThrows
    private void writeGradlePropertiesToDisk() {
        var properties = new Properties();
        gradleProperties.forEach((key, value) -> {
            var unwrappedKey = unwrapProviders(key);
            var unwrappedValue = unwrapProviders(value);
            if (unwrappedKey != null && unwrappedValue != null) {
                properties.setProperty(unwrappedKey.toString(), unwrappedValue.toString());
            }
        });

        var path = projectDir.toPath().resolve(GRADLE_PROPERTIES_RELATIVE_PATH);
        storeProperties(properties, path);
    }

    @OverridingMethodsMustInvokeSuper
    protected void writeToDisk() {
        writeTextFile(getBuildFileName(), buildFile.toString());
        writeGradlePropertiesToDisk();
    }


    @SneakyThrows
    public final void writeBinaryFile(String relativeFilePath, byte[] bytes) {
        if (GRADLE_PROPERTIES_RELATIVE_PATH.equals(relativeFilePath)) {
            throw new IllegalArgumentException(format(
                "Use methods of %s to set Gradle properties",
                this.getClass().getSimpleName()
            ));
        }

        var destPath = resolveRelativePath(relativeFilePath);
        createParentDirectories(destPath);
        write(destPath, bytes);
    }

    public final void writeTextFile(
        String relativeFilePath,
        String content,
        Charset charset
    ) {
        writeBinaryFile(relativeFilePath, content.getBytes(charset));
    }

    public final void writeTextFile(
        String relativeFilePath,
        String content
    ) {
        writeTextFile(relativeFilePath, content, DEFAULT_TEST_FILE_CHARSET);
    }

    public final void writeTextFile(
        String relativeFilePath,
        TextContent content,
        Charset charset
    ) {
        writeTextFile(relativeFilePath, content.toString(), charset);
    }

    public final void writeTextFile(
        String relativeFilePath,
        TextContent content
    ) {
        writeTextFile(relativeFilePath, content, DEFAULT_TEST_FILE_CHARSET);
    }

    public final void writeTextFile(
        String relativeFilePath,
        Action<? super TextContent> contentAction,
        Charset charset
    ) {
        var content = new TextContentDefault();
        contentAction.execute(content);
        writeTextFile(relativeFilePath, content, charset);
    }

    public final void writeTextFile(
        String relativeFilePath,
        Action<? super TextContent> contentAction
    ) {
        writeTextFile(relativeFilePath, contentAction, DEFAULT_TEST_FILE_CHARSET);
    }

    public final void writeJavaClassSourceFile(
        String relativeSourcesRootPath,
        JavaClassFileContent content,
        Charset charset
    ) {
        var relativeFilePath = new StringBuilder()
            .append(relativeSourcesRootPath).append("/");
        Optional.of(content.getPackageName())
            .filter(not(String::isEmpty))
            .ifPresent(packageName ->
                relativeFilePath.append(packageName.replace('.', '/')).append('/')
            );
        relativeFilePath.append(content.getSimpleName()).append(".java");
        writeTextFile(relativeFilePath.toString(), content.toString(), charset);
    }

    public final void writeJavaClassSourceFile(
        String relativeSourcesRootPath,
        JavaClassFileContent content
    ) {
        writeTextFile(relativeSourcesRootPath, content, DEFAULT_TEST_FILE_CHARSET);
    }

    public final void writeJavaClassSourceFile(
        String relativeSourcesRootPath,
        @Nullable String packageName,
        String simpleName,
        Action<? super JavaClassFileContent> contentAction,
        Charset charset
    ) {
        var content = new JavaClassFileContentDefault(packageName, simpleName);
        contentAction.execute(content);
        writeJavaClassSourceFile(relativeSourcesRootPath, content, charset);
    }

    public final void writeJavaClassSourceFile(
        String relativeSourcesRootPath,
        @Nullable String packageName,
        String simpleName,
        Action<? super JavaClassFileContent> contentAction
    ) {
        writeJavaClassSourceFile(relativeSourcesRootPath,
            packageName,
            simpleName,
            contentAction,
            DEFAULT_TEST_FILE_CHARSET);
    }


    @SneakyThrows
    public final byte[] readBinaryFile(String relativeFilePath) {
        var destPath = resolveRelativePath(relativeFilePath);
        return readAllBytes(destPath);
    }

    public final String readTextFile(String relativeFilePath, Charset charset) {
        var bytes = readBinaryFile(relativeFilePath);
        return new String(bytes, charset);
    }

    public final String readTextFile(String relativeFilePath) {
        return readTextFile(relativeFilePath, UTF_8);
    }


    public final Path resolveRelativePath(String relativeFilePath) {
        var relativePath = Paths.get(relativeFilePath);
        if (relativePath.isAbsolute()) {
            throw new IllegalArgumentException("Not a relative path: " + relativeFilePath);
        }

        var projectPath = normalizePath(projectDir.toPath());
        var destPath = normalizePath(projectPath.resolve(relativePath));
        if (!destPath.startsWith(projectPath)) {
            throw new IllegalArgumentException(
                "Relative path refers to a file outside of the project dir: " + relativeFilePath
            );
        }

        return destPath;
    }

}
