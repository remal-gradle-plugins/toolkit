package name.remal.gradle_plugins.toolkit.testkit.functional.generator.groovy;

import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.FoojayToolchainsResolverUtils.WITH_FOOJAY_TOOLCHAINS_RESOLVER_DEFAULT;
import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.FoojayToolchainsResolverUtils.applyFoojayToolchainsResolverConventionPlugin;

import lombok.Setter;

public class GradleSettingsFileContentGroovyDefault
    extends AbstractGradleFileContentGroovy
    implements GradleSettingsFileContentGroovy {

    @Setter
    private boolean withFoojayToolchainsResolver = WITH_FOOJAY_TOOLCHAINS_RESOLVER_DEFAULT;

    {
        applyFoojayToolchainsResolverConventionPlugin(this, () -> withFoojayToolchainsResolver);
    }

}
