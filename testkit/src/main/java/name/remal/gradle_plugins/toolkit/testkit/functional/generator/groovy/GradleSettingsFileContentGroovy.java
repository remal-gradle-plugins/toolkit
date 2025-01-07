package name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy;

import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.FoojayToolchainsResolverUtils.WITH_FOOJAY_TOOLCHAINS_RESOLVER_DEFAULT;
import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.FoojayToolchainsResolverUtils.applyFoojayToolchainsResolverConventionPlugin;

import lombok.Setter;
import name.remal.gradle_plugins.generate_sources.generators.java_like.groovy.GroovyContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleSettingsFileContent;

public class GradleSettingsFileContentGroovy
    extends GradleFileContentGroovy
    implements GradleSettingsFileContent<GroovyContent> {

    @Setter
    private boolean withFoojayToolchainsResolver = WITH_FOOJAY_TOOLCHAINS_RESOLVER_DEFAULT;

    {
        applyFoojayToolchainsResolverConventionPlugin(this, () -> withFoojayToolchainsResolver);
    }

}
