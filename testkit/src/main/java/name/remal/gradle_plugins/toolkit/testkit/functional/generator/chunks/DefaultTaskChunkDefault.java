package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import static java.lang.String.format;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;
import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;

import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

@RequiredArgsConstructor
public class DefaultTaskChunkDefault
    implements DefaultTaskChunk {

    private static final String DEFAULT_EMPTY_TASK_NAME = "_defaultEmptyTask";


    @Setter
    @Nullable
    private String defaultTask;


    @Override
    public String toString() {
        val defaultTask = this.defaultTask;
        if (isNotEmpty(defaultTask)) {
            return format(
                "project.defaultTasks(\"%s\")",
                escapeGroovy(defaultTask)
            );

        } else {
            return format(
                "project.defaultTasks(tasks.register(\"%s\").name)",
                DEFAULT_EMPTY_TASK_NAME
            );
        }
    }

}
