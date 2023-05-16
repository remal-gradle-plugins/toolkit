package name.remal.gradle_plugins.toolkit.testkit.functional;

import java.io.File;
import lombok.Getter;

@Getter
public class GradleChildProject extends AbstractGradleProject<GradleChildProject> {

    GradleChildProject(File projectDir) {
        super(projectDir);
    }

}
