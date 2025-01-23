package name.remal.gradle_plugins.toolkit.testkit.functional.generator;

import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.BuildDirMavenRepositories.getBuildDirMavenRepositories;

import java.nio.file.Path;
import java.time.Duration;
import javax.annotation.Nullable;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.DefaultTaskTimeoutChunk;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.WithDefaultTaskTimeout;

public interface GradleBuildFileContent<Block extends JavaLikeContent<Block>>
    extends GradleFileContent<Block>, WithDefaultTaskTimeout {

    default void addBuildDirMavenRepositories() {
        block("repositories", repos -> {
            getBuildDirMavenRepositories().stream()
                .map(Path::toUri)
                .forEach(uri -> repos.line("maven { url = \"%s\" }", escapeString(uri.toString())));
        });
    }

    @Override
    default void setDefaultTaskTimeout(@Nullable Duration defaultTaskTimeout) {
        getChunk(DefaultTaskTimeoutChunk.class).setDefaultTaskTimeout(defaultTaskTimeout);
    }

}
