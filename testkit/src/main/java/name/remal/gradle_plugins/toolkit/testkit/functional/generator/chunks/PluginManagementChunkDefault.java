package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import static name.remal.gradle_plugins.toolkit.CiUtils.isRunningOnCiIncludingTests;
import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.MavenCentralRepositoryUtils.addMavenCentralMirrorRepository;

import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;

public class PluginManagementChunkDefault<Block extends JavaLikeContent<Block>>
    implements PluginManagementChunk<Block> {

    @Getter
    private final Block pluginManagement;

    private final Supplier<Block> blockFactory;

    public PluginManagementChunkDefault(Supplier<Block> blockFactory) {
        this.pluginManagement = blockFactory.get();
        this.blockFactory = blockFactory;
    }


    @Setter
    private boolean withMavenCentralMirror = isRunningOnCiIncludingTests();


    @Override
    public String toString() {
        var content = blockFactory.get();
        content.block("pluginManagement", inner -> {
            if (withMavenCentralMirror) {
                addMavenCentralMirrorRepository(inner);
            }

            inner.line("repositories.gradlePluginPortal()");

            if (!pluginManagement.isEmpty()) {
                inner.line(pluginManagement);
            }
        });
        content.line();
        return content.toString();
    }

}
