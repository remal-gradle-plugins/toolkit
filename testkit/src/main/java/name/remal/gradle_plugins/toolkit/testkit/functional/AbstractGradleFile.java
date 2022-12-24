package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;
import static name.remal.gradle_plugins.toolkit.testkit.functional.BuildDirMavenRepositories.getBuildDirMavenRepositories;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

abstract class AbstractGradleFile<Child extends AbstractGradleFile<Child>> {

    protected final List<Object> chunks = new ArrayList<>();

    protected final File file;

    protected AbstractGradleFile(File file) {
        this.file = file.getAbsoluteFile();
    }


    @Contract("_ -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    public final Child append(CharSequence... contentParts) {
        chunks.addAll(asList(contentParts));
        return (Child) this;
    }


    @Contract("_ -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    public final Child applyPlugin(String pluginId) {
        getAppliedPluginsChunk().add(pluginId);
        return (Child) this;
    }

    @Contract("_,_ -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    public final Child applyPlugin(String pluginId, String version) {
        getAppliedPluginsChunk().add(pluginId, version);
        return (Child) this;
    }

    @UnmodifiableView
    public final Set<String> getAppliedPlugins() {
        return unmodifiableSet(getAppliedPluginsChunk().getPluginToVersion().keySet());
    }

    public final boolean isPluginApplied(String pluginId) {
        val appliedPlugins = getAppliedPlugins();
        return appliedPlugins.contains(pluginId)
            || appliedPlugins.contains("org.gradle." + pluginId);
    }

    private AppliedPluginsChunk getAppliedPluginsChunk() {
        for (val chunk : chunks) {
            if (chunk instanceof AppliedPluginsChunk) {
                return (AppliedPluginsChunk) chunk;
            }
        }

        val chunk = new AppliedPluginsChunk();
        chunks.add(0, chunk);
        return chunk;
    }


    @Contract(" -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings({"unchecked", "java:S3457"})
    public final Child appendBuildDirMavenRepositories() {
        val sb = new StringBuilder();
        sb.append("repositories {");
        getBuildDirMavenRepositories().forEach(repoPath -> {
            sb.append(format(
                "\n    maven { url = '%s' }",
                escapeGroovy(repoPath.toUri().toString())
            ));
        });
        sb.append("\n}");
        append(sb.toString());
        return (Child) this;
    }


    @SneakyThrows
    public final void writeToDisk() {
        createDirectories(requireNonNull(file.getParentFile()).toPath());
        write(file.toPath(), getContent().getBytes(UTF_8));
    }


    public final String getContent() {
        return chunks.stream()
            .map(Object::toString)
            .collect(joining("\n"));
    }

    @Override
    public final String toString() {
        return file.toString();
    }


    private static final class AppliedPluginsChunk {

        @Getter
        private final Map<String, String> pluginToVersion = new LinkedHashMap<>();

        public void add(String pluginId) {
            pluginToVersion.putIfAbsent(pluginId, null);
        }

        public void add(String pluginId, String pluginVersion) {
            pluginToVersion.put(pluginId, pluginVersion);
        }

        @Override
        public String toString() {
            if (pluginToVersion.isEmpty()) {
                return "";
            }

            val sb = new StringBuilder();
            sb.append("plugins {");
            pluginToVersion.forEach((pluginId, pluginVersion) -> {
                sb.append("\n    id '").append(pluginId).append("'");
                if (isNotEmpty(pluginVersion)) {
                    sb.append(" version '").append(pluginVersion).append("'");
                }
            });
            sb.append("\n}");
            return sb.toString();
        }

    }


}
