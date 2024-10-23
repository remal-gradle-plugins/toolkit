package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PUBLIC;
import static name.remal.gradle_plugins.toolkit.ReportContainerUtils.createReportContainerFor;
import static name.remal.gradle_plugins.toolkit.ReportUtils.getReportDestination;
import static name.remal.gradle_plugins.toolkit.ReportUtils.isReportEnabled;
import static name.remal.gradle_plugins.toolkit.testkit.TaskValidations.assertNoTaskPropertiesProblems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import groovy.lang.Closure;
import java.io.File;
import javax.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.CustomizableHtmlReport;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.testing.JUnitXmlReport;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ReportContainerUtilsTest {

    private final Project project;

    @Test
    void noTaskPropertiesProblems() {
        val task = project.getTasks().create(TestReportsTask.class.getSimpleName(), TestReportsTask.class);
        assertNoTaskPropertiesProblems(task);
    }

    @Test
    void reportsCorrectlyConfigured() {
        val task = project.getTasks().create(TestReportsTask.class.getSimpleName(), TestReportsTask.class);

        val baseReportsDir = project.file("build/reports/testReports/TestReportsTask");

        assertNotNull(task.reports.getFileXml());
        assertTrue(isReportEnabled(task.reports.getFileXml()));
        assertEquals(
            new File(baseReportsDir, "TestReportsTask.file.xml"),
            getReportDestination(task.reports.getFileXml())
        );

        assertNotNull(task.reports.getHtml());
        assertTrue(isReportEnabled(task.reports.getHtml()));
        assertEquals(
            new File(baseReportsDir, "TestReportsTask.html"),
            getReportDestination(task.reports.getHtml())
        );

        assertNotNull(task.reports.getDirectory());
        assertTrue(isReportEnabled(task.reports.getDirectory()));
        assertEquals(
            new File(baseReportsDir, "directory"),
            getReportDestination(task.reports.getDirectory())
        );
        assertEquals(
            new File(baseReportsDir, "directory/index.html"),
            task.reports.getDirectory().getEntryPoint()
        );

        assertNotNull(task.reports.getJunitXml());
        assertTrue(isReportEnabled(task.reports.getJunitXml()));
        assertEquals(
            new File(baseReportsDir, "junitXml"),
            getReportDestination(task.reports.getJunitXml())
        );
    }


    interface TestReportsContainer extends ReportContainer<ConfigurableReport> {

        @Internal
        SingleFileReport getFileXml();

        @Internal
        CustomizableHtmlReport getHtml();

        @Internal
        @DirectoryReportRelativeEntryPath("index.html")
        DirectoryReport getDirectory();

        @Internal
        JUnitXmlReport getJunitXml();

    }

    @NoArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
    static class TestReportsTask extends DefaultTask implements Reporting<TestReportsContainer> {

        @Getter(onMethod_ = {@Nested})
        private final TestReportsContainer reports = createReportContainerFor(this);

        @Override
        public TestReportsContainer reports(Closure closure) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestReportsContainer reports(Action<? super TestReportsContainer> configureAction) {
            throw new UnsupportedOperationException();
        }

    }

}
