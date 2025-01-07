package name.remal.gradle_plugins.toolkit.testkit.functional.generator;

import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;

public interface GradleSettingsFileContent<Block extends JavaLikeContent<Block>>
    extends GradleFileContent<Block> {

    void setWithFoojayToolchainsResolver(boolean withFoojayToolchainsResolver);

}
