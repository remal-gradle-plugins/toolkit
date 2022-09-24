package name.remal.gradleplugins.toolkit;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static name.remal.gradleplugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradleplugins.toolkit.PredicateUtils.not;
import static name.remal.gradleplugins.toolkit.ProjectUtils.getTopLevelDirOf;
import static name.remal.gradleplugins.toolkit.git.GitUtils.findGitRepositoryRootFor;
import static name.remal.gradleplugins.toolkit.git.GitUtils.getGitAttributesFor;
import static org.ec4j.core.Cache.Caches.permanent;
import static org.ec4j.core.model.PropertyType.charset;
import static org.ec4j.core.model.PropertyType.end_of_line;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.CustomLog;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import name.remal.gradleplugins.toolkit.git.GitStringAttribute;
import org.ec4j.core.Cache;
import org.ec4j.core.EditorConfigLoader;
import org.ec4j.core.PropertyTypeRegistry;
import org.ec4j.core.Resource.Resources;
import org.ec4j.core.ResourcePath.ResourcePaths;
import org.ec4j.core.ResourcePropertiesService;
import org.ec4j.core.model.Property;
import org.ec4j.core.model.Version;
import org.ec4j.core.parser.ErrorHandler;
import org.ec4j.core.parser.ParseException;
import org.gradle.api.Project;
import org.jetbrains.annotations.Unmodifiable;

@ToString(of = "rootPath")
@CustomLog
public final class EditorConfig {

    private static final ErrorHandler ERROR_HANDLER = (context, errorEvent) -> {
        val exception = new ParseException(errorEvent);
        if (errorEvent.getErrorType().isSyntaxError()) {
            throw exception;
        } else {
            logger.warn(".editorconfig parsing issue: {}", exception.getMessage());
        }
    };

    private static final EditorConfigLoader EDITOR_CONFIG_LOADER =
        EditorConfigLoader.of(Version.CURRENT, PropertyTypeRegistry.builder().build(), ERROR_HANDLER);

    private static final Cache CACHE = permanent();


    private final Path rootPath;
    private final ResourcePropertiesService service;

    public EditorConfig(Path rootPath) {
        this.rootPath = normalizePath(rootPath);

        this.service = ResourcePropertiesService.builder()
            .rootDirectory(ResourcePaths.ofPath(rootPath, UTF_8))
            .loader(EDITOR_CONFIG_LOADER)
            .keepUnset(false)
            .cache(CACHE)
            .build();
    }

    public EditorConfig(Project project) {
        this(getRootPathOf(project));
    }

    private static Path getRootPathOf(Project project) {
        val topLevelDir = getTopLevelDirOf(project);
        val repositoryRoot = findGitRepositoryRootFor(topLevelDir);
        if (repositoryRoot != null) {
            return repositoryRoot;
        }

        return topLevelDir;
    }


    @Unmodifiable
    public Map<String, String> getPropertiesForFileExtension(String extension) {
        if (extension.contains("/") || extension.contains("\\")) {
            throw new IllegalArgumentException("Not a file extension: " + extension);
        }

        return getPropertiesFor(rootPath.resolve("file." + extension));
    }

    @Unmodifiable
    public Map<String, String> getPropertiesFor(File file) {
        return getPropertiesFor(file.toPath());
    }

    @Unmodifiable
    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public Map<String, String> getPropertiesFor(Path path) {
        path = normalizePath(path);
        if (!path.startsWith(rootPath)) {
            throw new IllegalArgumentException(format(
                "Provided path is outside of root path (%s): %s",
                rootPath,
                path
            ));
        }

        synchronized (CACHE) {
            Map<String, String> result = new LinkedHashMap<>();

            val resourceProperties = service.queryProperties(Resources.ofPath(path, UTF_8));
            resourceProperties.getProperties().values().stream()
                .filter(not(Property::isUnset))
                .filter(Property::isValid)
                .forEach(property -> {
                    val name = property.getName();
                    val value = property.getSourceValue();
                    result.put(name, value);
                });

            val isCharsetNotSet = isEmpty(result.get(charset.getName()));
            val isEndOfLineNotSet = isEmpty(result.get(end_of_line.getName()));
            if (isCharsetNotSet || isEndOfLineNotSet) {
                val relativePath = rootPath.relativize(path).toString();
                getGitAttributesFor(rootPath, relativePath).stream()
                    .filter(GitStringAttribute.class::isInstance)
                    .map(GitStringAttribute.class::cast)
                    .forEach(attr -> {
                        if (attr.getName().equals("working-tree-encoding")) {
                            String encoding = attr.getValue().toLowerCase();
                            if (encoding.equals("utf-16")) {
                                encoding = "utf-16be";
                            }
                            if (charset.getPossibleValues().contains(encoding)) {
                                result.putIfAbsent(charset.getName(), encoding);
                            }
                        } else if (attr.getName().equals("eol")) {
                            val eol = attr.getValue().toLowerCase();
                            if (end_of_line.getPossibleValues().contains(eol)) {
                                result.putIfAbsent(end_of_line.getName(), eol);
                            }
                        }
                    });
            }

            return ImmutableMap.copyOf(result);
        }
    }

}
