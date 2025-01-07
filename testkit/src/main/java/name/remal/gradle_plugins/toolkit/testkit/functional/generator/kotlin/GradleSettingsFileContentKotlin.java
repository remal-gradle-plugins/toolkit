package name.remal.gradle_plugins.toolkit.testkit.functional.generator.kotlin;

import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.FoojayToolchainsResolverUtils.WITH_FOOJAY_TOOLCHAINS_RESOLVER_DEFAULT;
import static name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils.FoojayToolchainsResolverUtils.applyFoojayToolchainsResolverConventionPlugin;

import lombok.Setter;
import name.remal.gradle_plugins.generate_sources.generators.java_like.kotlin.KotlinContent;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleSettingsFileContent;

public class GradleSettingsFileContentKotlin
    extends GradleFileContentKotlin
    implements GradleSettingsFileContent<KotlinContent> {

    @Setter
    private boolean withFoojayToolchainsResolver = WITH_FOOJAY_TOOLCHAINS_RESOLVER_DEFAULT;

    {
        applyFoojayToolchainsResolverConventionPlugin(this, () -> withFoojayToolchainsResolver);
    }

}
