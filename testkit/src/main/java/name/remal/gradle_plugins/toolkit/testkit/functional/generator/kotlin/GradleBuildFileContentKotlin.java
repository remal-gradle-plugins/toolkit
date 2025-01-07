package name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin;

import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleBuildFileContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.DefaultTaskChunkDefault;

public class GradleBuildFileContentKotlin
    extends GradleChildBuildFileContentKotlin
    implements GradleBuildFileContent<KotlinContent> {

    public GradleBuildFileContentKotlin() {
        addLastChunks(
            new DefaultTaskChunkDefault()
        );
    }

}
