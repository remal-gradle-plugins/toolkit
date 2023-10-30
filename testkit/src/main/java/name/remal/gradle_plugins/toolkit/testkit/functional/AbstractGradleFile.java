package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static java.util.Collections.unmodifiableSet;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.unwrapProviders;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.generators.BaseGroovyFileContent;
import name.remal.gradle_plugins.toolkit.generators.GroovyFileContent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

abstract class AbstractGradleFile<
    Child extends AbstractGradleFile<Child>
    > extends BaseGroovyFileContent<Child> {

    protected final File file;

    protected AbstractGradleFile(File file) {
        this.file = normalizeFile(file);
    }

    @Override
    public final String toString() {
        return file.toString();
    }

    @SneakyThrows
    public final void writeToDisk() {
        createParentDirectories(file.toPath());
        write(file.toPath(), getContent().getBytes(UTF_8));
    }

    @Override
    @SuppressWarnings("MissingSuperCall")
    protected final void onPackageNameChanged(@Nullable String oldPackageName, @Nullable String newPackageName) {
        throw new UnsupportedOperationException("Gradle script can't have package name defined");
    }


    private final GroovyFileContent buildscript = new GroovyFileContent();

    {
        chunks.add((Supplier<Object>) () -> {
            val isNotEmptyBlock = new AtomicBoolean();
            val block = newBlock("buildscript", buildscriptBlock -> {
                val content = buildscript.getContent();
                buildscriptBlock.append(content);
                isNotEmptyBlock.set(isNotEmpty(content));
            });
            val content = block.getContent();
            return isNotEmptyBlock.get() ? content : null;
        });
    }

    public final GroovyFileContent getBuildscript() {
        return buildscript;
    }

    public final Child forBuildscript(Consumer<GroovyFileContent> action) {
        action.accept(buildscript);
        return getSelf();
    }


    private final Map<String, Object> pluginToVersion = new LinkedHashMap<>();

    {
        chunks.add((Supplier<Object>) () -> {
            val isNotEmptyBlock = new AtomicBoolean();
            val block = newBlock("plugins", pluginsBlock ->
                pluginToVersion.forEach((pluginId, pluginVersionObject) -> {
                    pluginVersionObject = unwrapProviders(pluginVersionObject);
                    if (pluginVersionObject == null) {
                        return;
                    }

                    String line = format("id '%s'", escapeGroovy(pluginId));
                    val pluginVersion = pluginVersionObject.toString();
                    if (isNotEmpty(pluginVersion)) {
                        line += format(" version '%s'", escapeGroovy(pluginVersion));
                    }
                    pluginsBlock.append(line);
                    isNotEmptyBlock.set(true);
                })
            );
            val content = block.getContent();
            return isNotEmptyBlock.get() ? content : null;
        });
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final Child applyPlugin(String pluginId) {
        pluginToVersion.put(pluginId, "");
        return getSelf();
    }

    @Contract("_,_ -> this")
    @CanIgnoreReturnValue
    public final Child applyPlugin(String pluginId, String version) {
        pluginToVersion.put(pluginId, version);
        return getSelf();
    }

    @Contract("_,_ -> this")
    @CanIgnoreReturnValue
    public final Child applyPlugin(String pluginId, Supplier<String> version) {
        pluginToVersion.put(pluginId, version);
        return getSelf();
    }

    @UnmodifiableView
    public final Set<String> getAppliedPlugins() {
        return unmodifiableSet(pluginToVersion.keySet());
    }

    public final boolean isPluginApplied(String pluginId) {
        val appliedPlugins = getAppliedPlugins();
        return appliedPlugins.contains(pluginId)
            || appliedPlugins.contains("org.gradle." + pluginId);
    }

}
