package name.remal.gradle_plugins.toolkit.tests_functional;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class SettingsAwarePluginFixtureFunctionalTest {

    final GradleProject project;

    @Test
    void appliedViaSettingsIsAppliedToProject() {
        project.forSettingsFile(settings -> settings.applyPlugin(
            "name.remal.toolkit.tests-functional.settings-aware-plugin-fixture"
        ));

        // The plugin must NOT be applied via the project's build file: it should reach the project
        // solely through the Settings-level application propagating via GradleLifecycle.beforeProject.
        project.assertBuildSuccessfully(SettingsAwarePluginFixture.MARKER_TASK_NAME);
    }

}
