package name.remal.gradleplugins.toolkit.testkit.functional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Contract;

abstract class AbstractGradleFile<Child extends AbstractGradleFile<Child>> {

    private final List<Object> chunks = new ArrayList<>();

    private final File file;

    protected AbstractGradleFile(File file) {
        this.file = file.getAbsoluteFile();
    }


    @Contract("_ -> this")
    @SuppressWarnings("unchecked")
    public final Child append(CharSequence... contentParts) {
        chunks.addAll(asList(contentParts));
        return (Child) this;
    }


    @Contract("_ -> this")
    @SuppressWarnings("unchecked")
    public final Child applyPlugin(String pluginId) {
        getAppliedPlugins().add(pluginId);
        return (Child) this;
    }

    @Contract("_,_ -> this")
    @SuppressWarnings("unchecked")
    public final Child applyPlugin(String pluginId, String version) {
        getAppliedPlugins().add(pluginId, version);
        return (Child) this;
    }

    private AppliedPlugins getAppliedPlugins() {
        for (val chunk : chunks) {
            if (chunk instanceof AppliedPlugins) {
                return (AppliedPlugins) chunk;
            }
        }

        val appliedPlugins = new AppliedPlugins();
        chunks.add(0, appliedPlugins);
        return appliedPlugins;
    }


    @SneakyThrows
    public final void writeToDisk() {
        createDirectories(requireNonNull(file.getParentFile()).toPath());
        Files.write(file.toPath(), getContent().getBytes(UTF_8));
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


    private static final class AppliedPlugins {

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
                if (pluginVersion != null && !pluginVersion.isEmpty()) {
                    sb.append(" version '").append(pluginVersion).append("'");
                }
            });
            sb.append("\n}");
            return sb.toString();
        }

    }


}
