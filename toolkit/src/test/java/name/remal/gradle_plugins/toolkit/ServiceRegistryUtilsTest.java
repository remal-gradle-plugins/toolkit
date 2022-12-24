package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ServiceRegistryUtils.getService;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.invocation.Gradle;
import org.gradle.execution.commandline.CommandLineTaskParser;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ServiceRegistryUtilsTest {

    private final Gradle gradle;

    @Test
    void gradle() {
        assertDoesNotThrow(() -> getService(gradle, CommandLineTaskParser.class));
    }

    @Test
    void project() {
        assertDoesNotThrow(() -> getService(gradle.getRootProject(), ConfigurationContainer.class));
    }

}
