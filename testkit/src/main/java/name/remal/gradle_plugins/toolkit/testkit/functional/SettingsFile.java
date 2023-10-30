package name.remal.gradle_plugins.toolkit.testkit.functional;

import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionGreaterThanOrEqualTo;
import static name.remal.gradle_plugins.toolkit.StringUtils.escapeGroovy;
import static name.remal.gradle_plugins.toolkit.testkit.functional.GradleSettingsPluginVersions.getSettingsBuildscriptClasspathDependencyVersion;

import java.io.File;
import lombok.Setter;
import org.gradle.util.GradleVersion;

public class SettingsFile extends AbstractGradleFile<SettingsFile> {

    private static final GradleVersion MIN_GRADLE_VERSION_WITH_TOOLCHAIN_RESOLVER = GradleVersion.version("7.6");


    SettingsFile(File projectDir) {
        super(new File(projectDir, "settings.gradle"));
        append("rootProject.name = '" + escapeGroovy(projectDir.getName().replace('.', '_')) + "'");
    }


    private static final boolean IS_TOOLCHAINS_RESOLVER_AVAILABLE =
        isCurrentGradleVersionGreaterThanOrEqualTo(MIN_GRADLE_VERSION_WITH_TOOLCHAIN_RESOLVER);

    @Setter
    private boolean withFoojayToolchainsResolver;

    {
        applyPlugin("org.gradle.toolchains.foojay-resolver-convention", () -> {
            if (IS_TOOLCHAINS_RESOLVER_AVAILABLE && withFoojayToolchainsResolver) {
                return getSettingsBuildscriptClasspathDependencyVersion("org.gradle.toolchains:foojay-resolver");
            }

            return null;
        });
    }

}
