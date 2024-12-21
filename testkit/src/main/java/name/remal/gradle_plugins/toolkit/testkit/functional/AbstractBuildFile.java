package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.testkit.functional.BuildDirMavenRepositories.getBuildDirMavenRepositories;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import javax.annotation.Nullable;
import name.remal.gradle_plugins.toolkit.StringUtils;
import org.jetbrains.annotations.Contract;

abstract class AbstractBuildFile<Child extends AbstractBuildFile<Child>>
    extends AbstractGradleFile<Child> {

    AbstractBuildFile(File projectDir) {
        super(new File(projectDir, "build.gradle"));
    }


    @Contract(" -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("java:S3457")
    public final Child addMavenCentralRepository() {
        append("repositories { mavenCentral() }");
        return getSelf();
    }

    @Contract(" -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("java:S3457")
    public final Child addBuildDirMavenRepositories() {
        append(newBlock(
            "repositories",
            getBuildDirMavenRepositories().stream()
                .map(Path::toUri)
                .map(Object::toString)
                .map(StringUtils::escapeGroovy)
                .map(uri -> format("maven { url = '%s' }", uri))
                .collect(toList())
        ));
        return getSelf();
    }


    @Nullable
    private Duration taskTimeout;

    {
        chunks.add(newBlock("tasks.configureEach", taskBlock -> taskBlock.append(
            taskTimeout != null
                ? format("timeout = Duration.parse('%s')", taskTimeout)
                : "// no timeout"
        )));
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final Child setTaskTimeout(@Nullable Duration timeout) {
        this.taskTimeout = timeout;
        return getSelf();
    }

}
