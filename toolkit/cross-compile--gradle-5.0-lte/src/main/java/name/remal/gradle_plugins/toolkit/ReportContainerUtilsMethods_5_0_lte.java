package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.internal.tasks.testing.DefaultJUnitXmlReport;
import org.gradle.api.reporting.CustomizableHtmlReport;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.reporting.internal.CustomizableHtmlReportImpl;
import org.gradle.api.reporting.internal.TaskGeneratedSingleDirectoryReport;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;
import org.gradle.api.reporting.internal.TaskReportContainer;
import org.gradle.api.tasks.testing.JUnitXmlReport;

@AutoService(ReportContainerUtilsMethods.class)
@ReliesOnInternalGradleApi
class ReportContainerUtilsMethods_5_0_lte implements ReportContainerUtilsMethods {

    @Override
    public <T extends Report> ReportContainer<T> createReportContainer(
        Task task,
        Class<? extends T> reportType,
        Action<ReportContainerConfigurer> configureAction
    ) {
        return new TaskReportContainerImpl<>(task, reportType, configureAction);
    }

    private static class TaskReportContainerImpl<T extends Report> extends TaskReportContainer<T> {

        @SuppressWarnings("unchecked")
        public TaskReportContainerImpl(
            Task task,
            Class<? extends T> reportType,
            Action<ReportContainerConfigurer> configureAction
        ) {
            super(
                reportType,
                task
            );

            configureAction.execute(new ReportContainerConfigurer() {
                @Override
                public SingleFileReport addSingleFileReport(String reportName) {
                    return (SingleFileReport) add(
                        (Class<T>) TaskGeneratedSingleFileReport.class,
                        reportName,
                        task
                    );
                }

                @Override
                public DirectoryReport addDirectoryReport(String reportName, @Nullable String relativeEntryPath) {
                    return (DirectoryReport) add(
                        (Class<T>) TaskGeneratedSingleDirectoryReport.class,
                        reportName,
                        task,
                        relativeEntryPath
                    );
                }

                @Override
                public CustomizableHtmlReport addCustomizableHtmlReport(String reportName) {
                    return (CustomizableHtmlReport) add(
                        (Class<T>) CustomizableHtmlReportImpl.class,
                        reportName,
                        task
                    );
                }

                @Override
                public JUnitXmlReport addJUnitXmlReport(String reportName) {
                    return (JUnitXmlReport) add(
                        (Class<T>) DefaultJUnitXmlReport.class,
                        reportName,
                        task
                    );
                }
            });
        }

    }

}
