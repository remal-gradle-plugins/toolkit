package name.remal.gradle_plugins.toolkit.testkit.functional;

import java.io.File;
import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy.GradleBuildFileContentGroovy;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy.GradleBuildFileContentGroovyDefault;

public class GradleChildProject
    extends AbstractChildGradleProject<GroovyContent, GradleBuildFileContentGroovy> {

    public GradleChildProject(File projectDir) {
        super(projectDir);
    }

    @Override
    protected final GradleBuildFileContentGroovy createBuildFileContent() {
        return new GradleBuildFileContentGroovyDefault();
    }

    @Override
    protected final String getBuildFileName() {
        return "build.gradle";
    }

}
