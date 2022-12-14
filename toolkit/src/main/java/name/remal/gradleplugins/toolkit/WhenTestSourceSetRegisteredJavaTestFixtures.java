package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;

import com.google.auto.service.AutoService;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

@AutoService(WhenTestSourceSetRegistered.class)
final class WhenTestSourceSetRegisteredJavaTestFixtures implements WhenTestSourceSetRegistered {

    @Override
    public void registerAction(Project project, Action<SourceSet> action) {
        project.getPluginManager().withPlugin("java-test-fixtures", __ -> {
            val sourceSets = getExtension(project, SourceSetContainer.class);
            val testFixturesSourceSet = sourceSets.getByName("testFixtures");
            action.execute(testFixturesSourceSet);
        });
    }

}
