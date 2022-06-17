package name.remal.gradleplugins.toolkit;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.findExtension;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizedPath;
import static name.remal.gradleplugins.toolkit.ReportUtils.setReportDestination;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.makeAccessible;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
import static org.gradle.api.reporting.Report.OutputType.DIRECTORY;
import static org.gradle.api.reporting.ReportingExtension.DEFAULT_REPORTS_DIR_NAME;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.TaskInputs;
import org.jetbrains.annotations.VisibleForTesting;

@NoArgsConstructor(access = PRIVATE)
public abstract class TaskUtils {

    public static boolean isInTaskGraph(Task task) {
        return task.getProject()
            .getGradle()
            .getTaskGraph()
            .hasTask(task);
    }

    public static boolean isRequested(Task task) {
        val project = task.getProject();

        val startParameter = project.getGradle().getStartParameter();
        val requestedProjectPath = Optional.ofNullable(startParameter.getProjectDir())
            .map(File::toPath)
            .map(PathUtils::normalizedPath)
            .orElse(null);
        if (requestedProjectPath != null) {
            val projectPath = normalizedPath(project.getProjectDir().toPath());
            if (!projectPath.startsWith(requestedProjectPath)) {
                return false;
            }
        }

        return startParameter.getTaskNames().stream()
            .anyMatch(task.getName()::equals);
    }

    public static void disableTask(Task task) {
        task.setEnabled(false);
        task.onlyIf(__ -> false);
        task.setDependsOn(emptyList());
        clearRegisteredFileProperties(task.getInputs());
    }

    @VisibleForTesting
    @SneakyThrows
    static void clearRegisteredFileProperties(TaskInputs taskInputs) {
        Class<?> taskInputsType = unwrapGeneratedSubclass(taskInputs.getClass());
        while (taskInputsType != Object.class) {
            try {
                val field = taskInputsType.getDeclaredField("registeredFileProperties");
                if (!isStatic(field.getModifiers()) && Iterable.class.isAssignableFrom(field.getType())) {
                    val properties = (Iterable<?>) makeAccessible(field).get(taskInputs);
                    val iterator = properties.iterator();
                    while (iterator.hasNext()) {
                        iterator.next();
                        iterator.remove();
                    }
                    break;
                }
            } catch (NoSuchFieldException e) {
                // do nothing
            }
            taskInputsType = taskInputsType.getSuperclass();
        }
    }


    public static <
        R extends Report, RC extends ReportContainer<R>, T extends Task & Reporting<RC>
        > void setTaskReportAutomaticDestinations(
        T task
    ) {
        setTaskReportAutomaticDestinations(task, Reporting::getReports);
    }

    public static <
        R extends Report, RC extends ReportContainer<R>, T extends Task & Reporting<RC>
        > void setTaskReportAutomaticDestinations(
        T task,
        Provider<File> baseReportsDirProvider
    ) {
        setTaskReportAutomaticDestinations(task, Reporting::getReports, baseReportsDirProvider);
    }

    public static <R extends Report, T extends Task> void setTaskReportAutomaticDestinations(
        T task,
        Function<? super T, ReportContainer<R>> reportsGetter
    ) {
        setTaskReportAutomaticDestinations(task, reportsGetter, newReportsDirProvider(task.getProject()));
    }

    @SuppressWarnings("unchecked")
    public static <R extends Report, T extends Task> void setTaskReportAutomaticDestinations(
        T task,
        Function<? super T, ReportContainer<R>> reportsGetter,
        Provider<File> baseReportsDirProvider
    ) {
        val reports = (ReportContainer<Report>) reportsGetter.apply(task);
        reports.withType(ConfigurableReport.class).all(report -> {
            setReportDestination(report, task.getProject().provider(() -> {
                val reportsDir = baseReportsDirProvider.get();
                val taskReportsDir = new File(reportsDir, task.getName());
                val reportFile = new File(
                    taskReportsDir,
                    report.getOutputType().equals(DIRECTORY)
                        ? report.getName()
                        : task.getName() + "." + report.getName()
                );
                return reportFile;
            }));
        });
    }

    private static Provider<File> newReportsDirProvider(Project project) {
        return project.provider(() -> {
            val reporting = findExtension(project, ReportingExtension.class);
            if (reporting != null) {
                val baseDir = reporting.getBaseDirectory().getAsFile().getOrNull();
                if (baseDir != null) {
                    return baseDir;
                }
            }

            return new File(project.getBuildDir(), DEFAULT_REPORTS_DIR_NAME);
        });
    }

}
