package name.remal.gradle_plugins.toolkit;

import static org.gradle.api.artifacts.Dependency.ARCHIVES_CONFIGURATION;
import static org.gradle.api.plugins.JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME;
import static org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME;
import static org.gradle.api.plugins.WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ConfigurationUtilsTest {

    private final Project project;

    @Test
    @MinSupportedGradleVersion("6.0")
    void isConfigurationDependenciesDeclarationDeprecated() {
        project.getPluginManager().apply(JavaPlugin.class);
        val implementation = project.getConfigurations().getByName(IMPLEMENTATION_CONFIGURATION_NAME);
        assertFalse(ConfigurationUtils.isConfigurationDependenciesDeclarationDeprecated(implementation));
        val compileClasspath = project.getConfigurations().getByName(COMPILE_CLASSPATH_CONFIGURATION_NAME);
        assertTrue(ConfigurationUtils.isConfigurationDependenciesDeclarationDeprecated(compileClasspath));
    }

    @Test
    @MinSupportedGradleVersion("7.1")
    void isConfigurationConsumptionDeprecated() {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(WarPlugin.class);
        val implementation = project.getConfigurations().getByName(IMPLEMENTATION_CONFIGURATION_NAME);
        assertFalse(ConfigurationUtils.isConfigurationConsumptionDeprecated(implementation));
        val providedCompile = project.getConfigurations().getByName(PROVIDED_COMPILE_CONFIGURATION_NAME);
        assertTrue(ConfigurationUtils.isConfigurationConsumptionDeprecated(providedCompile));
    }

    @Test
    @MinSupportedGradleVersion("6.3")
    void isConfigurationResolutionDeprecated() {
        project.getPluginManager().apply(JavaPlugin.class);
        val implementation = project.getConfigurations().getByName(IMPLEMENTATION_CONFIGURATION_NAME);
        assertFalse(ConfigurationUtils.isConfigurationResolutionDeprecated(implementation));
        val archives = project.getConfigurations().getByName(ARCHIVES_CONFIGURATION);
        assertTrue(ConfigurationUtils.isConfigurationResolutionDeprecated(archives));
    }

}
