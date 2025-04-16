package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import java.util.LinkedHashSet;
import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableGradleVersion;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableJavaVersion;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unbrokendome.gradle.plugins.testsets.dsl.TestSetContainer;

@MinTestableJavaVersion(11)
@MinTestableGradleVersion("7.0")
@RequiredArgsConstructor
class WhenTestSourceSetRegisteredUnbrokenDomeTestSetsTest {

    final WhenTestSourceSetRegisteredUnbrokenDomeTestSets handler
        = new WhenTestSourceSetRegisteredUnbrokenDomeTestSets();

    final Project project;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply("java");
        project.getPluginManager().apply("org.unbroken-dome.test-sets");
    }

    @Test
    void simple() {
        var testSourceSets = new LinkedHashSet<SourceSet>();
        handler.registerAction(project, testSourceSets::add);

        assertThat(testSourceSets)
            .extracting(SourceSet::getName).asInstanceOf(LIST)
            .containsExactlyInAnyOrder("test");
    }

    @Test
    void integration() {
        getExtension(project, TestSetContainer.class).create("integrationTest");

        var testSourceSets = new LinkedHashSet<SourceSet>();
        handler.registerAction(project, testSourceSets::add);

        assertThat(testSourceSets)
            .extracting(SourceSet::getName).asInstanceOf(LIST)
            .containsExactlyInAnyOrder("test", "integrationTest");
    }

}
