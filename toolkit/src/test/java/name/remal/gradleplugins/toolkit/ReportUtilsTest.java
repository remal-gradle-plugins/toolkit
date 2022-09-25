package name.remal.gradleplugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReportUtilsTest {

    private static final File TEMP_FILE1 = new File("1").getAbsoluteFile();
    private static final File TEMP_FILE2 = new File("2").getAbsoluteFile();


    private final Project project;
    private final SingleFileReport report;

    public ReportUtilsTest(Project project) {
        val reportTask = project.task("conventionTest");
        this.project = project;
        this.report = project.getObjects().newInstance(TaskGeneratedSingleFileReport.class, "test", reportTask);
    }


    @Nested
    class IsRequired {

        @Test
        void getAndSet() {
            assertFalse(ReportUtils.isReportEnabled(report));

            ReportUtils.setReportEnabled(report, true);
            assertTrue(ReportUtils.isReportEnabled(report));

            ReportUtils.setReportEnabled(report, false);
            assertFalse(ReportUtils.isReportEnabled(report));
        }

    }


    @Nested
    class Destination {

        @Test
        void getAndSet() {
            assertNull(ReportUtils.getReportDestination(report));

            ReportUtils.setReportDestination(report, TEMP_FILE1);
            assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(report));

            ReportUtils.setReportDestination(report, TEMP_FILE2);
            assertEquals(TEMP_FILE2, ReportUtils.getReportDestination(report));

            ReportUtils.setReportDestination(report, TEMP_FILE1);
            assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(report));
        }

        @Test
        void getAndSetProvider() {
            assertNull(ReportUtils.getReportDestination(report));

            ReportUtils.setReportDestination(report, project.provider(() -> TEMP_FILE1));
            assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(report));

            ReportUtils.setReportDestination(report, project.provider(() -> TEMP_FILE2));
            assertEquals(TEMP_FILE2, ReportUtils.getReportDestination(report));

            ReportUtils.setReportDestination(report, project.provider(() -> TEMP_FILE1));
            assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(report));
        }

    }

}
