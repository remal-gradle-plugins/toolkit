package name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy;

import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyContent;
import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyContentDefault;
import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyFileContentDefault;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleFileContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.BuildscriptChunkDefault;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.PluginsChunkDefault;

public abstract class AbstractGradleFileContentGroovy
    extends GroovyFileContentDefault
    implements GradleFileContent<GroovyContent> {

    protected AbstractGradleFileContentGroovy() {
        super(null, null);
        addLastChunks(
            new BuildscriptChunkDefault<>(GroovyContentDefault::new),
            new PluginsChunkDefault<>(GroovyContentDefault::new)
        );
    }

}
