package name.remal.gradle_plugins.toolkit;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import java.util.List;
import lombok.NoArgsConstructor;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ReportingExtension;

@NoArgsConstructor(access = PRIVATE)
public abstract class ReportingExtensionUtils {

    public static Provider<Directory> getTaskReportsDirProvider(Task task) {
        var project = task.getProject();
        project.getPluginManager().apply(ReportingBasePlugin.class);
        return project.getExtensions().getByType(ReportingExtension.class)
            .getBaseDirectory()
            .dir(getTaskTypeReportsDirName(task))
            .map(dir -> dir.dir(task.getName()));
    }

    private static final List<String> TASK_TYPE_REPORTS_DIR_NAME_SUFFIXES_TO_REMOVE = List.of(
        "Task"
    );

    private static String getTaskTypeReportsDirName(Task task) {
        String name = unwrapGeneratedSubclass(task.getClass()).getSimpleName();
        name = UPPER_CAMEL.to(LOWER_CAMEL, name);
        while (true) {
            boolean isChanged = false;
            for (var suffix : TASK_TYPE_REPORTS_DIR_NAME_SUFFIXES_TO_REMOVE) {
                if (!name.equals(suffix) && name.endsWith(suffix)) {
                    name = name.substring(0, name.length() - suffix.length());
                    isChanged = true;
                }
            }
            if (!isChanged) {
                break;
            }
        }
        return name;
    }


}
