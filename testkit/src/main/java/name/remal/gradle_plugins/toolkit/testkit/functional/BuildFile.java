package name.remal.gradle_plugins.toolkit.testkit.functional;

import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.File;
import org.jetbrains.annotations.Contract;

public class BuildFile extends AbstractBuildFile<BuildFile> {

    BuildFile(File projectDir) {
        super(projectDir);
    }


    {
        append(newBlock(
            "afterEvaluate",
            newBlock(
                "if (project.defaultTasks.isEmpty())",
                "project.defaultTasks(tasks.register('_defaultEmptyTask').name)"
            )
        ));
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public final BuildFile registerDefaultTask(String defaultTaskName) {
        append("project.defaultTasks('" + escapeGroovy(defaultTaskName) + "')");
        return getSelf();
    }

}
