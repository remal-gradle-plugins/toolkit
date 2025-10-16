package name.remal.gradle_plugins.toolkit;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;

interface ReportContainerUtilsMethods {

    <T extends Report> ReportContainer<T> createReportContainer(
        Task task,
        Class<? extends T> reportType,
        Action<? super ReportContainerConfigurer> configureAction
    );

}
