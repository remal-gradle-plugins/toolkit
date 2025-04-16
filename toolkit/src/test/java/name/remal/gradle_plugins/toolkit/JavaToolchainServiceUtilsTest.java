package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableGradleVersion;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@MinTestableGradleVersion("6.7")
@RequiredArgsConstructor
class JavaToolchainServiceUtilsTest {

    private final Project project;

    @Test
    void getJavaToolchainServiceFor() {
        assertNotNull(JavaToolchainServiceUtils.getJavaToolchainServiceFor(project));
    }

}
