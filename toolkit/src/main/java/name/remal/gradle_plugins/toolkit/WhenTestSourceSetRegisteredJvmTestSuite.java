package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;

import com.google.auto.service.AutoService;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testing.base.TestingExtension;

@AutoService(WhenTestSourceSetRegistered.class)
final class WhenTestSourceSetRegisteredJvmTestSuite implements WhenTestSourceSetRegistered {

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerAction(Project project, Action<SourceSet> action) {
        project.getPluginManager().withPlugin("jvm-test-suite", __ -> {
            var testing = getExtension(project, TestingExtension.class);
            var jvmSuites = testing.getSuites().withType(JvmTestSuite.class);
            jvmSuites.all(suite -> {
                var testSourceSet = suite.getSources();
                action.execute(testSourceSet);
            });
        });
    }

}
