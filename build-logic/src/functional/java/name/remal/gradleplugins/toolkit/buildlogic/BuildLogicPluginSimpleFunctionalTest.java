package name.remal.gradleplugins.toolkit.buildlogic;

import lombok.RequiredArgsConstructor;
import name.remal.gradleplugins.toolkit.testkit.functional.GradleProject;
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
