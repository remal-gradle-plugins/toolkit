package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;

import com.google.auto.service.AutoService;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

@AutoService(WhenTestSourceSetRegistered.class)
final class WhenTestSourceSetRegisteredRemalTestSourceSets implements WhenTestSourceSetRegistered {

    @Override
    public void registerAction(Project project, Action<SourceSet> action) {
        project.getPluginManager().withPlugin("name.remal.test-source-sets", __ -> {
            val testSourceSetsExtension = getExtension(project, "testSourceSets");
            @SuppressWarnings("unchecked")
            val testSourceSetsContainer = (NamedDomainObjectContainer<Object>) testSourceSetsExtension;
            testSourceSetsContainer.withType(SourceSet.class).all(action);
        });
    }

}
