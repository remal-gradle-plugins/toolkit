package name.remal.gradleplugins.toolkit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static name.remal.gradleplugins.toolkit.LayoutUtils.getRootPathOf;
import static name.remal.gradleplugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradleplugins.toolkit.PredicateUtils.not;
import static name.remal.gradleplugins.toolkit.git.GitUtils.getGitAttributesFor;
import static org.ec4j.core.Cache.Caches.permanent;
import static org.ec4j.core.model.PropertyType.charset;
import static org.ec4j.core.model.PropertyType.end_of_line;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
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
import org.ec4j.core.model.PropertyType;
import org.ec4j.core.model.PropertyType.EndOfLineValue;
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
            .rootDirectory(ResourcePaths.ofPath(this.rootPath, UTF_8))
            .loader(EDITOR_CONFIG_LOADER)
            .keepUnset(false)
            .cache(CACHE)
            .build();
    }

    public EditorConfig(Project project) {
        this(getRootPathOf(project));
    }


    @Unmodifiable
    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public Map<String, String> getPropertiesFor(Path path) {
        path = normalizePath(path);
        if (!path.startsWith(rootPath)) {
            throw new PathIsOutOfRootPathException(path, rootPath);
        }

        synchronized (CACHE) {
            Map<String, String> result = new LinkedHashMap<>();

            val resourceProperties = service.queryProperties(Resources.ofPath(path, UTF_8));
            resourceProperties.getProperties().values().stream()
                .filter(not(Property::isUnset))
                .filter(Property::isValid)
                .forEach(property -> {
                    val name = property.getName();
                    result.remove(name);
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

    @Unmodifiable
    public Map<String, String> getPropertiesFor(File file) {
        return getPropertiesFor(file.toPath());
    }

    @Unmodifiable
    public Map<String, String> getPropertiesForFileExtension(String extension) {
        return getPropertiesFor(getExamplePathFileExtension(extension));
    }


    public String getLineSeparatorFor(Path path) {
        String result = getPropertyFor(path, end_of_line, value -> {
            for (val endOfLine : EndOfLineValue.values()) {
                if (endOfLine.name().equalsIgnoreCase(value)) {
                    return endOfLine.getEndOfLineString();
                }
            }
            return null;
        });
        if (isEmpty(result)) {
            result = "\n";
        }
        return result;
    }

    public String getLineSeparatorFor(File file) {
        return getLineSeparatorFor(file.toPath());
    }

    public String getLineSeparatorForFileExtension(String extension) {
        return getLineSeparatorFor(getExamplePathFileExtension(extension));
    }


    public Charset getCharsetFor(Path path) {
        Charset result = getPropertyFor(path, charset, value -> {
            try {
                return Charset.forName(value);
            } catch (UnsupportedCharsetException ignored) {
                return null;
            }
        });
        if (result == null) {
            result = UTF_8;
        }
        return result;
    }

    public Charset getCharsetFor(File file) {
        return getCharsetFor(file.toPath());
    }

    public Charset getCharsetForFileExtension(String extension) {
        return getCharsetFor(getExamplePathFileExtension(extension));
    }


    private Path getExamplePathFileExtension(String extension) {
        if (isEmpty(extension) || extension.contains("/") || extension.contains("\\")) {
            throw new IllegalArgumentException("Not a file extension: " + extension);
        }

        return rootPath.resolve("file." + extension);
    }

    @FunctionalInterface
    private interface PropertyValueConverter<T> {
        @Nullable
        T convert(String value) throws Throwable;

    }

    @Nullable
    @SneakyThrows
    private <T> T getPropertyFor(
        Path path,
        PropertyType<?> property,
        PropertyValueConverter<T> converter
    ) {
        val properties = getPropertiesFor(path);
        val value = properties.get(property.getName());
        return value != null ? converter.convert(value) : null;
    }

}
