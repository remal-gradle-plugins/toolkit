package name.remal.gradle_plugins.toolkit.tests_functional;

import static lombok.AccessLevel.PUBLIC;
import static name.remal.gradle_plugins.toolkit.ReportContainerUtils.createReportContainerFor;

import groovy.lang.Closure;
import javax.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;

@CacheableTask
@NoArgsConstructor(access = PUBLIC, onConstructor_ = {@Inject})
public abstract class TestReportsTask extends DefaultTask implements Reporting<TestReports> {

    @Getter(onMethod_ = {@Nested})
    private final TestReports reports = createReportContainerFor(this);

    @TaskAction
    public void execute() {
        // no-op: only needs to run, to exercise configuration cache store+reload of `reports`
    }

    @Override
    public TestReports reports(Closure closure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestReports reports(Action<? super TestReports> configureAction) {
        configureAction.execute(reports);
        return reports;
    }

}
