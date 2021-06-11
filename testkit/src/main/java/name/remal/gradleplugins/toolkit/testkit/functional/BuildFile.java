package name.remal.gradleplugins.toolkit.testkit.functional;

import static name.remal.gradleplugins.toolkit.StringEscapeUtils.escapeGroovy;

import java.io.File;
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
        ).replace(" ", ""));
    }

    @Contract("_ -> this")
    public final BuildFile registerDefaultTask(String defaultTaskName) {
        append("project.defaultTasks('" + escapeGroovy(defaultTaskName) + "')");
        return this;
    }

}
