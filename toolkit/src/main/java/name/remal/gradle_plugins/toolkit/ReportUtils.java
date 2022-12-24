package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;

import java.io.File;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;

@NoArgsConstructor(access = PRIVATE)
public abstract class ReportUtils {

    private static final ReportUtilsMethods METHODS = loadCrossCompileService(ReportUtilsMethods.class);


    public static boolean isReportEnabled(Report report) {
        return METHODS.isReportEnabled(report);
    }

    public static void setReportEnabled(Report report, boolean enabled) {
        METHODS.setReportEnabled(report, enabled);
    }


    @Nullable
    public static File getReportDestination(Report report) {
        return METHODS.getReportDestination(report);
    }

    public static void setReportDestination(ConfigurableReport report, Provider<File> fileProvider) {
        METHODS.setReportDestination(report, fileProvider);
    }

    public static void setReportDestination(ConfigurableReport report, File file) {
        METHODS.setReportDestination(report, file);
    }

}
