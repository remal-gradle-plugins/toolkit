package name.remal.gradleplugins.toolkit;

import com.google.auto.service.AutoService;
import java.io.File;
import javax.annotation.Nullable;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;

@AutoService(ReportUtilsMethods.class)
final class ReportUtilsMethods_6_0 implements ReportUtilsMethods {

    @Override
    public boolean isReportEnabled(Report report) {
        return report.isEnabled();
    }

    @Override
    public void setReportEnabled(Report report, boolean enabled) {
        report.setEnabled(enabled);
    }

    @Nullable
    @Override
    public File getReportDestination(Report report) {
        return report.getDestination();
    }

    @Override
    public void setReportDestination(ConfigurableReport report, Provider<File> fileProvider) {
        report.setDestination(fileProvider);
    }

    @Override
    public void setReportDestination(ConfigurableReport report, File file) {
        report.setDestination(file);
    }

}
