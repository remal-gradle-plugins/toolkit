package name.remal.gradle_plugins.toolkit.tests_functional;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class TestReportsTaskFunctionalTest {

    final GradleProject project;

    @Test
    void taskWithCustomReportsSurvivesConfigurationCacheReload() {
        project.forBuildFile(build -> {
            build.applyPlugin("name.remal.toolkit.tests-functional");
            build.addImport(TestReportsTask.class);
            build.line("tasks.register('testReports', %s)", TestReportsTask.class.getSimpleName());
        });

        project.assertBuildSuccessfully("testReports");
    }

}
