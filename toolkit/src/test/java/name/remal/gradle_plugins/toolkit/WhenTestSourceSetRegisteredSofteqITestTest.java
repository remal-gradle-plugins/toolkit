package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.testkit.ProjectAfterEvaluateActionsExecutor.executeAfterEvaluateActions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import com.softeq.gradle.itest.ItestSourceSetExtension;
import java.util.LinkedHashSet;
import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MinSupportedGradleVersion("7.0")
@RequiredArgsConstructor
class WhenTestSourceSetRegisteredSofteqITestTest {

    final WhenTestSourceSetRegisteredSofteqITest handler = new WhenTestSourceSetRegisteredSofteqITest();

    final Project project;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply("java");
        project.getPluginManager().apply("com.softeq.gradle.itest");
    }

    @Test
    void noCustomName() {
        val testSourceSets = new LinkedHashSet<SourceSet>();
        handler.registerAction(project, testSourceSets::add);

        assertThat(testSourceSets)
            .extracting(SourceSet::getName).asInstanceOf(LIST)
            .containsExactlyInAnyOrder("itest");
    }

    @Test
    void customNameBeforeEvaluate() {
        getExtension(project, ItestSourceSetExtension.class).setName("itestCustom");

        val testSourceSets = new LinkedHashSet<SourceSet>();
        handler.registerAction(project, testSourceSets::add);

        assertThat(testSourceSets)
            .extracting(SourceSet::getName).asInstanceOf(LIST)
            .containsExactlyInAnyOrder("itest");
    }

    @Test
    void customNameAfterEvaluate() {
        getExtension(project, ItestSourceSetExtension.class).setName("itestCustom");

        val testSourceSets = new LinkedHashSet<SourceSet>();
        handler.registerAction(project, testSourceSets::add);

        executeAfterEvaluateActions(project);

        assertThat(testSourceSets)
            .extracting(SourceSet::getName).asInstanceOf(LIST)
            .containsExactlyInAnyOrder("itest", "itestCustom");
    }

}
