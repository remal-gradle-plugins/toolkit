package name.remal.gradleplugins.toolkit;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

public interface WhenTestSourceSetRegistered {

    void registerAction(Project project, Action<SourceSet> action);

}
