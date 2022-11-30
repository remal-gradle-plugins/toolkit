package name.remal.gradleplugins.toolkit.tasks;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static name.remal.gradleplugins.toolkit.ClosureUtils.configureWith;
import static name.remal.gradleplugins.toolkit.ReportContainerUtils.createReportContainerFor;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Action;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.VerificationTask;

@Setter
public abstract class BaseSourceVerificationReportingTask<Reports extends ReportContainer<?>>
    extends SourceTask
    implements VerificationTask, Reporting<Reports> {

    private boolean ignoreFailures;

    @Getter(onMethod_ = {@Nested})
    private final Reports reports = createReportContainerFor(this);

    @Override
    @Internal
    public boolean getIgnoreFailures() {
        return ignoreFailures;
    }

    @Override
    public Reports reports(@DelegatesTo(strategy = DELEGATE_FIRST) Closure closure) {
        configureWith(reports, closure);
        return reports;
    }

    @Override
    public Reports reports(Action<? super Reports> configureAction) {
        configureAction.execute(reports);
        return reports;
    }

}
