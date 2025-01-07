package name.remal.gradle_plugins.toolkit.testkit.functional;

import java.io.File;
import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy.GradleChildBuildFileContentGroovy;

public class GradleChildProject
    extends AbstractChildGradleProject<GroovyContent, GradleChildBuildFileContentGroovy> {

    public GradleChildProject(File projectDir) {
        super(projectDir);
    }

    @Override
    protected GradleChildBuildFileContentGroovy createBuildFileContent() {
        return new GradleChildBuildFileContentGroovy();
    }

    @Override
    protected String getBuildFileName() {
        return "build.gradle";
    }

}
