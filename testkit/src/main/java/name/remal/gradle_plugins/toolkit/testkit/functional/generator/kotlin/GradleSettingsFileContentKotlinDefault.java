package name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin;

import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.FoojayToolchainsResolverUtils.WITH_FOOJAY_TOOLCHAINS_RESOLVER_DEFAULT;
import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.FoojayToolchainsResolverUtils.applyFoojayToolchainsResolverConventionPlugin;

import lombok.Setter;

public class GradleSettingsFileContentKotlinDefault
    extends AbstractGradleFileContentKotlin
    implements GradleSettingsFileContentKotlin {

    @Setter
    private boolean withFoojayToolchainsResolver = WITH_FOOJAY_TOOLCHAINS_RESOLVER_DEFAULT;

    {
        applyFoojayToolchainsResolverConventionPlugin(this, () -> withFoojayToolchainsResolver);
    }

}
