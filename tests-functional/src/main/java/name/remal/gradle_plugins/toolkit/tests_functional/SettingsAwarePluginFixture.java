package name.remal.gradle_plugins.toolkit.tests_functional;

import name.remal.gradle_plugins.toolkit.AbstractSettingsAwarePlugin;
import org.gradle.api.Project;

/**
 * Test-only fixture for functionally testing {@link AbstractSettingsAwarePlugin}.
 */
public class SettingsAwarePluginFixture extends AbstractSettingsAwarePlugin {

    public static final String MARKER_TASK_NAME = "settingsAwarePluginFixtureApplied";

    @Override
    protected void applyToProject(Project project) {
        project.getTasks().register(MARKER_TASK_NAME);
    }

}
