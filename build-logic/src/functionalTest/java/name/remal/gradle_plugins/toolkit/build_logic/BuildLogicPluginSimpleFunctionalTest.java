package name.remal.gradle_plugins.toolkit.build_logic;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class BuildLogicPluginSimpleFunctionalTest {

    private final GradleProject project;

    @Test
    void applyPlugin() {
        project.forBuildFile(build -> {
            build.applyPlugin("name.remal.toolkit.build-logic");
            build.registerDefaultTask("allClasses");
        });
        project.withoutConfigurationCache();
        project.assertBuildSuccessfully();
    }

}
