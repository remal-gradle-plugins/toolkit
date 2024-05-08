package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeMethod;

import com.google.auto.service.AutoService;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

@AutoService(WhenTestSourceSetRegistered.class)
final class WhenTestSourceSetRegisteredSofteqITest implements WhenTestSourceSetRegistered {

    @Override
    public void registerAction(Project project, Action<SourceSet> action) {
        project.getPluginManager().withPlugin("com.softeq.gradle.itest", __ -> {
            val sourceSets = getExtension(project, SourceSetContainer.class);
            sourceSets.matching(sourceSet -> {
                if (sourceSet.getName().equals("itest")) {
                    return true;
                }

                val itestSourceSet = getExtension(project, "itestSourceSet");
                val name = invokeMethod(itestSourceSet, String.class, "getName");
                return sourceSet.getName().equals(name);
            }).all(action);
        });
    }

}
