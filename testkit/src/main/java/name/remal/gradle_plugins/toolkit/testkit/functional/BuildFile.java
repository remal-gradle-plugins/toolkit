package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;
import static name.remal.gradle_plugins.toolkit.testkit.functional.BuildDirMavenRepositories.getBuildDirMavenRepositories;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import javax.annotation.Nullable;
import name.remal.gradle_plugins.toolkit.StringUtils;
import org.jetbrains.annotations.Contract;

public class BuildFile extends AbstractGradleFile<BuildFile> {

    BuildFile(File projectDir) {
        super(new File(projectDir, "build.gradle"));
        append(newBlock(
            "afterEvaluate",
            newBlock(
                "if (project.defaultTasks.isEmpty())",
                "project.defaultTasks(tasks.create('_defaultEmptyTask').name)"
            )
        ));
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final BuildFile registerDefaultTask(String defaultTaskName) {
        append("project.defaultTasks('" + escapeGroovy(defaultTaskName) + "')");
        return getSelf();
    }


    @Contract(" -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("java:S3457")
    public final BuildFile addMavenCentralRepository() {
        append("repositories { mavenCentral() }");
        return getSelf();
    }

    @Contract(" -> this")
    @CanIgnoreReturnValue
    @SuppressWarnings("java:S3457")
    public final BuildFile addBuildDirMavenRepositories() {
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
    public final BuildFile setTaskTimeout(@Nullable Duration timeout) {
        this.taskTimeout = timeout;
        return getSelf();
    }

}
