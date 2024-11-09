package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeMethod;

import com.google.auto.service.AutoService;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnExternalDependency;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

@AutoService(WhenTestSourceSetRegistered.class)
@ReliesOnExternalDependency
final class WhenTestSourceSetRegisteredUnbrokenDomeTestSets implements WhenTestSourceSetRegistered {

    @Override
    public void registerAction(Project project, Action<SourceSet> action) {
        if (!JavaVersion.current().isJava11Compatible()) {
            return;
        }

        project.getPluginManager().withPlugin("org.unbroken-dome.test-sets", __ -> {
            val testSetsExtension = getExtension(project, "testSets");
            @SuppressWarnings("unchecked")
            val testSets = (NamedDomainObjectContainer<Object>) testSetsExtension;
            testSets.all(testSet -> {
                val testSourceSet = invokeMethod(testSet, SourceSet.class, "getSourceSet");
                action.execute(testSourceSet);
            });
        });
    }

}
