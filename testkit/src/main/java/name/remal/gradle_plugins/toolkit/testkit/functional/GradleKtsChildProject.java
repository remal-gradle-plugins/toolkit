package name.remal.gradle_plugins.toolkit.testkit.functional;

import java.io.File;
import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin.GradleChildBuildFileContentKotlin;

public class GradleKtsChildProject
    extends AbstractChildGradleProject<KotlinContent, GradleChildBuildFileContentKotlin> {

    public GradleKtsChildProject(File projectDir) {
        super(projectDir);
    }

    @Override
    protected GradleChildBuildFileContentKotlin createBuildFileContent() {
        return new GradleChildBuildFileContentKotlin();
    }

    @Override
    protected String getBuildFileName() {
        return "build.gradle.kts";
    }

}
