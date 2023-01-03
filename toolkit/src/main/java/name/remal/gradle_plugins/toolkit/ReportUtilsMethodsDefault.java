package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ObjectUtils.defaultFalse;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.getMethod;

import com.google.auto.service.AutoService;
import java.io.File;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradle_plugins.toolkit.reflection.TypedMethod0;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemLocationProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;

@AutoService(ReportUtilsMethods.class)
final class ReportUtilsMethodsDefault implements ReportUtilsMethods {

    @Override
    public boolean isReportEnabled(Report report) {
        return defaultFalse(report.getRequired().getOrNull());
    }

    @Override
    public void setReportEnabled(Report report, boolean enabled) {
        report.getRequired().set(enabled);
    }


    // TODO: remove when Gradle 8.0 is released
    @SuppressWarnings("rawtypes")
    private static final TypedMethod0<Report, Provider> getOutputLocationMethod =
        getMethod(Report.class, Provider.class, "getOutputLocation");

    @Nullable
    @Override
    public File getReportDestination(Report report) {
        val outputLocation = getOutputLocationMethod.invoke(report);
        val outputFileSystemLocation = (FileSystemLocation) outputLocation.getOrNull();
        return outputFileSystemLocation != null ? outputFileSystemLocation.getAsFile() : null;
    }

    @Override
    public void setReportDestination(ConfigurableReport report, Provider<File> fileProvider) {
        val outputLocation = getOutputLocationMethod.invoke(report);
        val outputLocationProperty = (FileSystemLocationProperty<?>) outputLocation;
        outputLocationProperty.fileProvider(fileProvider);
    }

    @Override
    public void setReportDestination(ConfigurableReport report, File file) {
        val outputLocation = getOutputLocationMethod.invoke(report);
        val outputLocationProperty = (FileSystemLocationProperty<?>) outputLocation;
        outputLocationProperty.fileValue(file);
    }

}
