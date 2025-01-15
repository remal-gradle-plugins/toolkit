package name.remal.gradle_plugins.toolkit.testkit.functional;

import java.io.File;
import lombok.Getter;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleBuildFileContent;

@Getter
public abstract class AbstractChildGradleProject<
    Block extends JavaLikeContent<Block>,
    BuildFileType extends GradleBuildFileContent<Block>
    > extends AbstractBaseGradleProject<Block, BuildFileType> {

    protected AbstractChildGradleProject(File projectDir) {
        super(projectDir);
    }

}
