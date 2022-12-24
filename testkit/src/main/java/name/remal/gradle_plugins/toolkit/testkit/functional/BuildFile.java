package name.remal.gradle_plugins.toolkit.testkit.functional;

import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.File;
import java.time.Duration;
import javax.annotation.Nullable;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.Contract;

public class BuildFile extends AbstractGradleFile<BuildFile> {

    BuildFile(File projectDir) {
        super(new File(projectDir, "build.gradle"));
        append((
            "afterEvaluate {"
                + "    if (project.defaultTasks.isEmpty()) {"
                + "        project.defaultTasks(tasks.create('_defaultEmptyTask').name)"
                + "    }"
                + "}"
        ).replaceAll("\\s+", " "));
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final BuildFile registerDefaultTask(String defaultTaskName) {
        append("project.defaultTasks('" + escapeGroovy(defaultTaskName) + "')");
        return this;
    }


    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final BuildFile setTaskTimeout(@Nullable Duration timeout) {
        getTaskTimeoutChunk().setTimeout(timeout);
        return this;
    }


    private TaskTimeoutChunk getTaskTimeoutChunk() {
        for (val chunk : chunks) {
            if (chunk instanceof TaskTimeoutChunk) {
                return (TaskTimeoutChunk) chunk;
            }
        }

        val chunk = new TaskTimeoutChunk();
        chunks.add(chunk);
        return chunk;
    }

    @Setter
    private static class TaskTimeoutChunk {

        @Nullable
        private Duration timeout;

        @Override
        public String toString() {
            val timeout = this.timeout;
            if (timeout == null) {
                return "";
            }

            return "tasks.configureEach { timeout = Duration.parse('" + escapeGroovy(timeout.toString()) + "') }";
        }

    }


}
