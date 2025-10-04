package name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionGreaterThanOrEqualTo;
import static name.remal.gradle_plugins.toolkit.testkit.functional.GradleSettingsPluginVersions.getSettingsBuildscriptClasspathDependencyVersion;

import java.util.function.BooleanSupplier;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.GradleSettingsFileContent;

@NoArgsConstructor(access = PRIVATE)
public abstract class FoojayToolchainsResolverUtils {

    public static final boolean WITH_FOOJAY_TOOLCHAINS_RESOLVER_DEFAULT = true;

    private static final boolean IS_TOOLCHAINS_RESOLVER_AVAILABLE = isCurrentGradleVersionGreaterThanOrEqualTo("7.6");

    private static final String FOOJAY_TOOLCHAINS_RESOLVER_CONVENTION_PLUGIN_ID =
        "org.gradle.toolchains.foojay-resolver-convention";

    private static final String FOOJAY_TOOLCHAINS_RESOLVER_CONVENTION_PLUGIN_VERSION =
        getSettingsBuildscriptClasspathDependencyVersion("org.gradle.toolchains:foojay-resolver");

    public static void applyFoojayToolchainsResolverConventionPlugin(
        GradleSettingsFileContent<?> settingsFile,
        BooleanSupplier isEnabled
    ) {
        settingsFile.applyPlugin(FOOJAY_TOOLCHAINS_RESOLVER_CONVENTION_PLUGIN_ID, () -> {
            if (IS_TOOLCHAINS_RESOLVER_AVAILABLE && isEnabled.getAsBoolean()) {
                return FOOJAY_TOOLCHAINS_RESOLVER_CONVENTION_PLUGIN_VERSION;
            }
            return null;
        });
    }

}
