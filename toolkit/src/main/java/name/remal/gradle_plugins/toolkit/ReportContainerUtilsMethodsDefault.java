package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.tasks.testing.DefaultJUnitXmlReport;
import org.gradle.api.reporting.CustomizableHtmlReport;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.reporting.internal.CustomizableHtmlReportImpl;
import org.gradle.api.reporting.internal.DefaultReportContainer;
import org.gradle.api.reporting.internal.DefaultSingleFileReport;
import org.gradle.api.reporting.internal.SingleDirectoryReport;
import org.gradle.api.tasks.testing.JUnitXmlReport;
import org.gradle.internal.Describables;

@AutoService(ReportContainerUtilsMethods.class)
@ReliesOnInternalGradleApi
class ReportContainerUtilsMethodsDefault implements ReportContainerUtilsMethods {

    @Override
    @SuppressWarnings({"unchecked", "java:S1854"})
    public <T extends Report> ReportContainer<T> createReportContainer(
        Task task,
        Class<? extends T> reportType,
        Action<ReportContainerConfigurer> configureAction
    ) {
        val owner = Describables.quoted("Task", ((TaskInternal) task).getIdentityPath());

        return DefaultReportContainer.create(
            task.getProject().getObjects(),
            reportType,
            factory -> {
                val reports = new ArrayList<T>();
                configureAction.execute(new ReportContainerConfigurer() {
                    private <R extends Report> R add(Class<R> type, Object... args) {
                        val report = factory.instantiateReport((Class<T>) type, args);
                        reports.add(report);
                        return (R) report;
                    }

                    @Override
                    public SingleFileReport addSingleFileReport(String reportName) {
                        return add(
                            DefaultSingleFileReport.class,
                            reportName,
                            owner
                        );
                    }

                    @Override
                    public DirectoryReport addDirectoryReport(String reportName, @Nullable String relativeEntryPath) {
                        return add(
                            SingleDirectoryReport.class,
                            reportName,
                            owner,
                            relativeEntryPath
                        );
                    }

                    @Override
                    public CustomizableHtmlReport addCustomizableHtmlReport(String reportName) {
                        return add(
                            CustomizableHtmlReportImpl.class,
                            reportName,
                            owner
                        );
                    }

                    @Override
                    public JUnitXmlReport addJUnitXmlReport(String reportName) {
                        return add(
                            DefaultJUnitXmlReport.class,
                            reportName,
                            owner
                        );
                    }
                });
                return reports;
            }
        );
    }

}
