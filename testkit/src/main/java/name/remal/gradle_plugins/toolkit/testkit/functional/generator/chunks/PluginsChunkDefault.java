package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import static java.util.Collections.unmodifiableSet;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.unwrapProviders;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import org.jetbrains.annotations.UnmodifiableView;

@RequiredArgsConstructor
public class PluginsChunkDefault<Block extends JavaLikeContent<Block>>
    implements PluginsChunk {

    private final Supplier<Block> blockFactory;


    private final Map<String, Object> pluginToVersion = new LinkedHashMap<>();

    @Override
    public void applyPlugin(String pluginId, @Nullable Object version) {
        pluginToVersion.put(pluginId, version);
    }

    @Override
    public void applyPluginAtTheBeginning(String pluginId, @Nullable Object version) {
        val currentPlugins = new LinkedHashMap<>(pluginToVersion);
        pluginToVersion.clear();
        pluginToVersion.put(pluginId, version);
        pluginToVersion.putAll(currentPlugins);
    }

    @Override
    @UnmodifiableView
    public Set<String> getAppliedPlugins() {
        val appliedPlugins = new LinkedHashSet<>(pluginToVersion.keySet());
        pluginToVersion.forEach((pluginId, versionObj) -> {
            versionObj = unwrapProviders(versionObj);
            if (versionObj != null) {
                appliedPlugins.add(pluginId);
            }
        });
        return unmodifiableSet(appliedPlugins);
    }

    @Override
    public String toString() {
        if (pluginToVersion.isEmpty()) {
            return "";
        }

        val content = blockFactory.get();
        content.block("plugins", plugins -> {
            pluginToVersion.forEach((pluginId, versionObj) -> {
                versionObj = unwrapProviders(versionObj);
                if (versionObj == null) {
                    return;
                }

                val decl = new StringBuilder();
                decl.append("id(\"").append(plugins.escapeString(pluginId)).append("\")");
                val version = versionObj.toString();
                if (isNotEmpty(versionObj)) {
                    decl.append(" version \"").append(plugins.escapeString(version)).append("\"");
                }
                plugins.line(decl.toString());
            });
        });
        return content.toString();
    }

}
