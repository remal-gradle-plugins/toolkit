package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import java.io.File;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemLocationProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.jspecify.annotations.Nullable;

@AutoService(ReportUtilsMethods.class)
final class ReportUtilsMethodsDefault implements ReportUtilsMethods {

    @Override
    public boolean isReportEnabled(Report report) {
        return report.getRequired().getOrElse(false);
    }

    @Override
    public void setReportEnabled(Report report, boolean enabled) {
        report.getRequired().set(enabled);
    }


    @Nullable
    @Override
    public File getReportDestination(Report report) {
        var outputLocation = report.getOutputLocation();
        var outputFileSystemLocation = (FileSystemLocation) outputLocation.getOrNull();
        return outputFileSystemLocation != null ? outputFileSystemLocation.getAsFile() : null;
    }

    @Override
    public void setReportDestination(ConfigurableReport report, Provider<File> fileProvider) {
        var outputLocation = report.getOutputLocation();
        var outputLocationProperty = (FileSystemLocationProperty<?>) outputLocation;
        outputLocationProperty.fileProvider(fileProvider);
    }

    @Override
    public void setReportDestination(ConfigurableReport report, File file) {
        var outputLocation = report.getOutputLocation();
        var outputLocationProperty = (FileSystemLocationProperty<?>) outputLocation;
        outputLocationProperty.fileValue(file);
    }

}
