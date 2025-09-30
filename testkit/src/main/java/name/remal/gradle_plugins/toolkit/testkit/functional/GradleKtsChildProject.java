package name.remal.gradle_plugins.toolkit.testkit.functional;

import java.io.File;
import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin.GradleBuildFileContentKotlin;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin.GradleBuildFileContentKotlinDefault;

public class GradleKtsChildProject
    extends AbstractChildGradleProject<KotlinContent, GradleBuildFileContentKotlin> {

    public GradleKtsChildProject(File projectDir) {
        super(projectDir);
    }

    @Override
    protected final GradleBuildFileContentKotlin createBuildFileContent() {
        return new GradleBuildFileContentKotlinDefault();
    }

    @Override
    protected final String getBuildFileName() {
        return "build.gradle.kts";
    }

}
