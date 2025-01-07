package name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin;

import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinContent;
import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinContentDefault;
import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinFileContentDefault;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleFileContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.BuildscriptChunkDefault;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.PluginsChunkDefault;

public abstract class AbstractGradleFileContentKotlin
    extends KotlinFileContentDefault
    implements GradleFileContent<KotlinContent> {

    protected AbstractGradleFileContentKotlin() {
        super(null, null);
        addLastChunks(
            new BuildscriptChunkDefault<>(KotlinContentDefault::new),
            new PluginsChunkDefault<>(KotlinContentDefault::new)
        );
    }

}
