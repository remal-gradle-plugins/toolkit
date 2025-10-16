package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import javax.annotation.Nullable;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.testing.DefaultJUnitXmlReport;
import org.gradle.api.model.ObjectFactory;
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
class ReportContainerUtilsMethods_8_10_lte implements ReportContainerUtilsMethods {

    @Override
    public <T extends Report> ReportContainer<T> createReportContainer(
        Task task,
        Class<? extends T> reportType,
        Action<? super ReportContainerConfigurer> configureAction
    ) {
        return new TaskReportContainerImpl<>(task, reportType, configureAction);
    }

    private static class TaskReportContainerImpl<T extends Report> extends TaskReportContainer<T> {

        @SuppressWarnings("unchecked")
        public TaskReportContainerImpl(
            Task task,
            Class<? extends T> reportType,
            Action<? super ReportContainerConfigurer> configureAction
        ) {
            super(
                reportType,
                task,
                ((ProjectInternal) task.getProject())
                    .getServices()
                    .get(CollectionCallbackActionDecorator.class)
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
                    if (HAS_DEFAULT_JUNIT_XML_REPORT_OBJECT_FACTORY) {
                        return (JUnitXmlReport) add(
                            (Class<T>) DefaultJUnitXmlReport.class,
                            reportName,
                            task,
                            task.getProject().getObjects()
                        );

                    } else {
                        return (JUnitXmlReport) add(
                            (Class<T>) DefaultJUnitXmlReport.class,
                            reportName,
                            task
                        );
                    }
                }
            });
        }

    }

    private static final boolean HAS_DEFAULT_JUNIT_XML_REPORT_OBJECT_FACTORY = hasDefaultJUnitXmlReportObjectFactory();

    @SuppressWarnings("ReturnValueIgnored")
    private static boolean hasDefaultJUnitXmlReportObjectFactory() {
        try {
            DefaultJUnitXmlReport.class.getConstructor(String.class, Task.class, ObjectFactory.class);
            return true;

        } catch (NoSuchMethodException e) {
            return false;
        }
    }

}
