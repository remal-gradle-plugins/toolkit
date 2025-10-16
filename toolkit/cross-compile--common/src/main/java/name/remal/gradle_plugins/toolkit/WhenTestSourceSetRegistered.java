package name.remal.gradle_plugins.toolkit;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

public interface WhenTestSourceSetRegistered {

    void registerAction(Project project, Action<? super SourceSet> action);

}
