package name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy;

import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleBuildFileContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.DefaultTaskChunkDefault;

public class GradleBuildFileContentGroovy
    extends GradleChildBuildFileContentGroovy
    implements GradleBuildFileContent<GroovyContent> {

    public GradleBuildFileContentGroovy() {
        addLastChunks(
            new DefaultTaskChunkDefault()
        );
    }

}
