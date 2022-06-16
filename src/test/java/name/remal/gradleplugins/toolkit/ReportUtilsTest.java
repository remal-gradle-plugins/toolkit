package name.remal.gradleplugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.gradle.api.Project;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.SingleFileReport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReportUtilsTest {

    private static final File TEMP_FILE1 = new File("1").getAbsoluteFile();
    private static final File TEMP_FILE2 = new File("2").getAbsoluteFile();


    private final Project project;
    private final SingleFileReport singleFileReport;
    private final DirectoryReport directoryReport;

    public ReportUtilsTest(Project project) {
        this.project = project;
        this.singleFileReport = project.getTasks().create("checkstyle", org.gradle.api.plugins.quality.Checkstyle.class)
            .getReports().getXml();
        this.directoryReport = project.getTasks().create("test", org.gradle.api.tasks.testing.Test.class)
            .getReports().getHtml();
    }


    @Nested
    class IsRequired {

        @Test
        void singleFile() {
            ReportUtils.setReportEnabled(singleFileReport, true);
            assertTrue(ReportUtils.isReportEnabled(singleFileReport));

            ReportUtils.setReportEnabled(singleFileReport, false);
            assertFalse(ReportUtils.isReportEnabled(singleFileReport));

            ReportUtils.setReportEnabled(singleFileReport, true);
            assertTrue(ReportUtils.isReportEnabled(singleFileReport));
        }

        @Test
        void directory() {
            ReportUtils.setReportEnabled(directoryReport, true);
            assertTrue(ReportUtils.isReportEnabled(directoryReport));

            ReportUtils.setReportEnabled(directoryReport, false);
            assertFalse(ReportUtils.isReportEnabled(directoryReport));

            ReportUtils.setReportEnabled(directoryReport, true);
            assertTrue(ReportUtils.isReportEnabled(directoryReport));
        }

    }


    @Nested
    class Destination {

        @Nested
        class ByProvider {

            @Test
            void singleFile() {
                ReportUtils.setReportDestination(singleFileReport, project.provider(() -> TEMP_FILE1));
                assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(singleFileReport));

                ReportUtils.setReportDestination(singleFileReport, project.provider(() -> TEMP_FILE2));
                assertEquals(TEMP_FILE2, ReportUtils.getReportDestination(singleFileReport));

                ReportUtils.setReportDestination(singleFileReport, project.provider(() -> TEMP_FILE1));
                assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(singleFileReport));
            }

            @Test
            void directory() {
                ReportUtils.setReportDestination(directoryReport, project.provider(() -> TEMP_FILE1));
                assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(directoryReport));

                ReportUtils.setReportDestination(directoryReport, project.provider(() -> TEMP_FILE2));
                assertEquals(TEMP_FILE2, ReportUtils.getReportDestination(directoryReport));

                ReportUtils.setReportDestination(directoryReport, project.provider(() -> TEMP_FILE1));
                assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(directoryReport));
            }

        }


        @Nested
        class ByFile {

            @Test
            void singleFile() {
                ReportUtils.setReportDestination(singleFileReport, TEMP_FILE1);
                assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(singleFileReport));

                ReportUtils.setReportDestination(singleFileReport, TEMP_FILE2);
                assertEquals(TEMP_FILE2, ReportUtils.getReportDestination(singleFileReport));

                ReportUtils.setReportDestination(singleFileReport, TEMP_FILE1);
                assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(singleFileReport));
            }

            @Test
            void directory() {
                ReportUtils.setReportDestination(directoryReport, TEMP_FILE1);
                assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(directoryReport));

                ReportUtils.setReportDestination(directoryReport, TEMP_FILE2);
                assertEquals(TEMP_FILE2, ReportUtils.getReportDestination(directoryReport));

                ReportUtils.setReportDestination(directoryReport, TEMP_FILE1);
                assertEquals(TEMP_FILE1, ReportUtils.getReportDestination(directoryReport));
            }

        }

    }

}
