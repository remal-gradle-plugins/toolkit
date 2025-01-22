package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeMethod;

import com.google.auto.service.AutoService;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnExternalDependency;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

@AutoService(WhenTestSourceSetRegistered.class)
@ReliesOnExternalDependency
final class WhenTestSourceSetRegisteredSofteqITest implements WhenTestSourceSetRegistered {

    @Override
    public void registerAction(Project project, Action<SourceSet> action) {
        project.getPluginManager().withPlugin("com.softeq.gradle.itest", __ -> {
            var sourceSets = getExtension(project, SourceSetContainer.class);
            sourceSets.matching(sourceSet -> {
                if (sourceSet.getName().equals("itest")) {
                    return true;
                }

                var itestSourceSet = getExtension(project, "itestSourceSet");
                var name = invokeMethod(itestSourceSet, String.class, "getName");
                return sourceSet.getName().equals(name);
            }).all(action);
        });
    }

}
