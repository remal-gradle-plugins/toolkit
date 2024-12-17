package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertFalse;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ConfigurationUtilsTest {

    final Project project;

    @Test
    void isDeprecatedForConsumption() {
        val conf = project.getConfigurations().create("conf", it ->
            it.setCanBeConsumed(true)
        );
        assertFalse(ConfigurationUtils.isDeprecatedForConsumption(conf), "isDeprecatedForConsumption");
    }

    @Test
    void isDeprecatedForResolution() {
        val conf = project.getConfigurations().create("conf", it ->
            it.setCanBeResolved(true)
        );
        assertFalse(ConfigurationUtils.isDeprecatedForResolution(conf), "isDeprecatedForResolution");
    }

}