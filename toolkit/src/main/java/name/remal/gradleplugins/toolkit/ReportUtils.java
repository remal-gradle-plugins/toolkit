package name.remal.gradleplugins.toolkit;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.getMethod;

import java.io.File;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradleplugins.toolkit.reflection.TypedMethod0;
import name.remal.gradleplugins.toolkit.reflection.TypedVoidMethod1;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;

@NoArgsConstructor(access = PRIVATE)
public abstract class ReportUtils {

    @Nullable
    @SuppressWarnings("rawtypes")
    private static final TypedMethod0<Report, Property> getRequiredMethod =
        findMethod(Report.class, Property.class, "getRequired");

    @Nullable
    private static final TypedMethod0<Report, Boolean> isEnabledMethod =
        findMethod(Report.class, Boolean.class, "isEnabled");

    public static boolean isReportEnabled(Report report) {
        if (getRequiredMethod != null) {
            val required = requireNonNull(getRequiredMethod.invoke(report));
            return TRUE.equals(required.getOrNull());

        } else {
            val isEnabledMethod = requireNonNull(
                ReportUtils.isEnabledMethod,
                "isEnabledMethod"
            );
            return TRUE.equals(isEnabledMethod.invoke(report));
        }
    }


    @Nullable
    private static final TypedVoidMethod1<Report, Boolean> setEnabledMethod =
        findMethod(Report.class, "setEnabled", Boolean.class);

    @SuppressWarnings("unchecked")
    public static void setReportEnabled(Report report, boolean enabled) {
        if (getRequiredMethod != null) {
            val required = requireNonNull(getRequiredMethod.invoke(report));
            required.set(enabled);

        } else {
            val setEnabledMethod = requireNonNull(
                ReportUtils.setEnabledMethod,
                "setEnabledMethod"
            );
            setEnabledMethod.invoke(report, enabled);
        }
    }


    @Nullable
    @SuppressWarnings("rawtypes")
    private static final TypedMethod0<Report, Provider> getOutputLocationMethod =
        findMethod(Report.class, Provider.class, "getOutputLocation");

    @Nullable
    @SuppressWarnings("rawtypes")
    private static final TypedVoidMethod1<ConfigurableReport, Provider> setDestinationProviderMethod =
        findMethod(ConfigurableReport.class, "setDestination", Provider.class);

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void setReportDestination(ConfigurableReport report, Provider<File> fileProvider) {
        if (getOutputLocationMethod != null) {
            val outputLocation = requireNonNull(getOutputLocationMethod.invoke(report));
            val fileProviderMethod = getMethod(
                (Class<Provider>) outputLocation.getClass(),
                "fileProvider",
                Provider.class
            );
            fileProviderMethod.invoke(outputLocation, fileProvider);

        } else {
            val setDestinationProviderMethod = requireNonNull(
                ReportUtils.setDestinationProviderMethod,
                "setDestinationProviderMethod"
            );
            setDestinationProviderMethod.invoke(report, fileProvider);
        }
    }

    @Nullable
    private static final TypedVoidMethod1<ConfigurableReport, File> setDestinationFileMethod =
        findMethod(ConfigurableReport.class, "setDestination", File.class);

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void setReportDestination(ConfigurableReport report, File file) {
        if (getOutputLocationMethod != null) {
            val outputLocation = requireNonNull(getOutputLocationMethod.invoke(report));
            val fileValueMethod = getMethod(
                (Class<Provider>) outputLocation.getClass(),
                "fileValue",
                File.class
            );
            fileValueMethod.invoke(outputLocation, file);

        } else {
            val setDestinationFileMethod = requireNonNull(
                ReportUtils.setDestinationFileMethod,
                "setDestinationFileMethod"
            );
            setDestinationFileMethod.invoke(report, file);
        }
    }


    @Nullable
    private static final TypedMethod0<Report, File> getDestinationMethod =
        findMethod(Report.class, File.class, "getDestination");

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static File getReportDestination(Report report) {
        if (getOutputLocationMethod != null) {
            val outputLocation = requireNonNull(getOutputLocationMethod.invoke(report));
            val getAsFileMethod = getMethod(
                (Class<Provider>) outputLocation.getClass(),
                Provider.class,
                "getAsFile"
            );
            val fileProvider = requireNonNull(getAsFileMethod.invoke(outputLocation));
            return (File) fileProvider.getOrNull();

        } else {
            val getDestinationMethod = requireNonNull(
                ReportUtils.getDestinationMethod,
                "getDestinationMethod"
            );
            return getDestinationMethod.invoke(report);
        }
    }

}
