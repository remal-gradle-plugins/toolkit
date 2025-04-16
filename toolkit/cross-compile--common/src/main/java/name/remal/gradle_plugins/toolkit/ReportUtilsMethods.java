package name.remal.gradle_plugins.toolkit;

import java.io.File;
import javax.annotation.Nullable;
import name.remal.gradle_plugins.toolkit.annotations.DynamicCompatibilityCandidate;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;

@DynamicCompatibilityCandidate
interface ReportUtilsMethods {

    boolean isReportEnabled(Report report);

    void setReportEnabled(Report report, boolean enabled);


    @Nullable
    File getReportDestination(Report report);

    void setReportDestination(ConfigurableReport report, Provider<File> fileProvider);

    void setReportDestination(ConfigurableReport report, File file);

}
