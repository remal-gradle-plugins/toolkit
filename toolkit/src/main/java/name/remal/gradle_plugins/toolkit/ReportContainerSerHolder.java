package name.remal.gradle_plugins.toolkit;

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import lombok.NoArgsConstructor;
import org.gradle.api.reporting.ReportContainer;

@NoArgsConstructor(access = PRIVATE)
abstract class ReportContainerSerHolder {

    /**
     * Stable stand-in for a runtime-generated {@link ReportContainerUtils} report container wrapper,
     * so that Gradle's configuration cache never needs to resolve the generated wrapper class by name.
     *
     * <p>This class is public only because the runtime-generated wrapper it replaces lives in the
     * report container interface's own package, which requires cross-package access to construct it.
     */
    @NoArgsConstructor(access = PRIVATE, force = true)
    public static final class ReportContainerSer implements Serializable {

        private final Class<? extends ReportContainer<?>> reportContainerType;
        private final ReportContainer<?> delegate;

        @SuppressWarnings("unused")
        public ReportContainerSer(
            Class<? extends ReportContainer<?>> reportContainerType,
            ReportContainer<?> delegate
        ) {
            this.reportContainerType = reportContainerType;
            this.delegate = delegate;
        }

        private Object readResolve() {
            return ReportContainerUtils.withReportGetters(
                requireNonNull(reportContainerType),
                requireNonNull(delegate)
            );
        }

    }

}
