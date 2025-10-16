package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;

import com.google.auto.service.AutoService;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

@AutoService(WhenTestSourceSetRegistered.class)
final class WhenTestSourceSetRegisteredJavaTestFixtures implements WhenTestSourceSetRegistered {

    @Override
    public void registerAction(Project project, Action<? super SourceSet> action) {
        project.getPluginManager().withPlugin("java-test-fixtures", __ -> {
            var sourceSets = getExtension(project, SourceSetContainer.class);
            var testFixturesSourceSet = sourceSets.getByName("testFixtures");
            action.execute(testFixturesSourceSet);
        });
    }

}
