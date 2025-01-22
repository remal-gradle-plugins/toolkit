package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;

import com.google.auto.service.AutoService;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnExternalDependency;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

@AutoService(WhenTestSourceSetRegistered.class)
@ReliesOnExternalDependency
final class WhenTestSourceSetRegisteredRemalTestSourceSets implements WhenTestSourceSetRegistered {

    @Override
    public void registerAction(Project project, Action<SourceSet> action) {
        project.getPluginManager().withPlugin("name.remal.test-source-sets", __ -> {
            var testSourceSetsExtension = getExtension(project, "testSourceSets");
            @SuppressWarnings("unchecked")
            var testSourceSetsContainer = (NamedDomainObjectContainer<Object>) testSourceSetsExtension;
            testSourceSetsContainer.withType(SourceSet.class).all(action);
        });
    }

}
