package name.remal.gradle_plugins.toolkit.testkit.functional;

import java.io.File;
import lombok.Getter;

@Getter
public class GradleChildProject extends AbstractGradleProject<GradleChildProject, ChildBuildFile> {

    GradleChildProject(File projectDir) {
        super(projectDir);
    }

    @Override
    protected ChildBuildFile createBuildFile(File projectDir) {
        return new ChildBuildFile(projectDir);
    }

}
