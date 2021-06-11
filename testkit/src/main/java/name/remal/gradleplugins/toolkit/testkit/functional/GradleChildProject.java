package name.remal.gradleplugins.toolkit.testkit.functional;

import java.io.File;
import lombok.Getter;

@Getter
public class GradleChildProject extends BaseGradleProject<GradleChildProject> {

    GradleChildProject(File projectDir) {
        super(projectDir);
    }

}
