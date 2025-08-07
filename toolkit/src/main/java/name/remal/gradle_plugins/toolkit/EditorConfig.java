package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static name.remal.gradle_plugins.toolkit.LayoutUtils.getRootPathOf;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.git.GitUtils.getGitAttributesFor;
import static org.ec4j.core.Cache.Caches.permanent;
import static org.ec4j.core.model.PropertyType.charset;
import static org.ec4j.core.model.PropertyType.end_of_line;

import com.google.common.collect.ImmutableMap;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import name.remal.gradle_plugins.toolkit.git.GitStringAttribute;
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
import org.gradle.api.Project;
import org.jetbrains.annotations.Unmodifiable;

@ToString(of = "rootPath")
@CustomLog
@SuppressWarnings("java:S1948")
public final class EditorConfig implements Serializable {

    private static final ErrorHandler ERROR_HANDLER = (context, errorEvent) -> {
        var message = format(
            "%s:%d:%d: %s",
            errorEvent.getResource(),
            errorEvent.getStart().getLine(),
            errorEvent.getStart().getColumn(),
            errorEvent.getMessage()
        );
        logger.debug(".editorconfig parsing issue: {}", message);
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

            var resourceProperties = service.queryProperties(Resources.ofPath(path, UTF_8));
            resourceProperties.getProperties().values().stream()
                .filter(not(Property::isUnset))
                .filter(Property::isValid)
                .forEach(property -> {
                    var name = property.getName();
                    result.remove(name);
                    var value = property.getSourceValue();
                    result.put(name, value);
                });

            var isCharsetNotSet = isEmpty(result.get(charset.getName()));
            var isEndOfLineNotSet = isEmpty(result.get(end_of_line.getName()));
            if (isCharsetNotSet || isEndOfLineNotSet) {
                var relativePath = rootPath.relativize(path).toString();
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
                            var eol = attr.getValue().toLowerCase();
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
            for (var endOfLine : EndOfLineValue.values()) {
                if (endOfLine.name().equalsIgnoreCase(value)) {
                    return endOfLine.getEndOfLineString();
                }
            }
            return null;
        });

        if (result == null || result.isEmpty()) {
            return "\n";
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
        var properties = getPropertiesFor(path);
        var value = properties.get(property.getName());
        return value != null ? converter.convert(value) : null;
    }


    // region serialization

    private Object writeReplace() {
        return new EditorConfigSer(rootPath.toUri(), null);
    }

    private void readObject(ObjectInputStream inputStream) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    private static class EditorConfigSer implements Externalizable {

        @Nullable
        private URI rootPathUri;

        @Nullable
        private EditorConfig deserializedObject;

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(rootPathUri);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            rootPathUri = (URI) in.readObject();
            var rootPath = Paths.get(rootPathUri);
            deserializedObject = new EditorConfig(rootPath);
        }

        private Object readResolve() {
            return requireNonNull(deserializedObject);
        }

    }

    // endregion

}
