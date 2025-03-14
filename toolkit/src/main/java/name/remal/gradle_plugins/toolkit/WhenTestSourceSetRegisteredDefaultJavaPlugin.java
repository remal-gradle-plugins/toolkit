package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME;

import com.google.auto.service.AutoService;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

@AutoService(WhenTestSourceSetRegistered.class)
final class WhenTestSourceSetRegisteredDefaultJavaPlugin implements WhenTestSourceSetRegistered {

    @Override
    public void registerAction(Project project, Action<SourceSet> action) {
        project.getPluginManager().withPlugin("java", __ -> {
            var sourceSets = getExtension(project, SourceSetContainer.class);
            var testSourceSet = sourceSets.getByName(TEST_SOURCE_SET_NAME);
            action.execute(testSourceSet);
        });
    }

}
