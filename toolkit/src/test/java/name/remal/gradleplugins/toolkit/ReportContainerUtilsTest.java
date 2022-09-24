package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.ReportContainerUtils.createReportContainerFor;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.CustomizableHtmlReport;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.testing.JUnitXmlReport;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ReportContainerUtilsTest {

    private final Project project;

    @Test
    void test() {
        val task = project.getTasks().create(TestReportsTask.class.getSimpleName(), TestReportsTask.class);
        assertNotNull(task.reports.getFile());
        assertNotNull(task.reports.getHtml());
        assertNotNull(task.reports.getDirectory());
        assertNotNull(task.reports.getJunitXml());
    }


    interface TestReportsContainer extends ReportContainer<ConfigurableReport> {

        SingleFileReport getFile();

        CustomizableHtmlReport getHtml();

        DirectoryReport getDirectory();

        JUnitXmlReport getJunitXml();

    }

    public static class TestReportsTask extends DefaultTask {

        private final TestReportsContainer reports = createReportContainerFor(this, TestReportsContainer.class);

    }

}
