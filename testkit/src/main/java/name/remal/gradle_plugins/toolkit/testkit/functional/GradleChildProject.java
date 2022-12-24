package name.remal.gradle_plugins.toolkit.testkit.functional;

import java.io.File;
import lombok.Getter;

@Getter
public class GradleChildProject extends BaseGradleProject<GradleChildProject> {

    GradleChildProject(File projectDir) {
        super(projectDir);
    }

}
