package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class DependencyUtilsTest {

    final Project project;

    @Test
    void platform() {
        var dependency = project.getDependencies().platform("test:test");
        assertTrue(DependencyUtils.isPlatformDependency(dependency));
        assertFalse(DependencyUtils.isEnforcedPlatformDependency(dependency));
        assertFalse(DependencyUtils.isDocumentationDependency(dependency));
    }

    @Test
    void enforcedPlatform() {
        var dependency = project.getDependencies().enforcedPlatform("test:test");
        assertTrue(DependencyUtils.isPlatformDependency(dependency));
        assertTrue(DependencyUtils.isEnforcedPlatformDependency(dependency));
        assertFalse(DependencyUtils.isDocumentationDependency(dependency));
    }


    @Test
    @MinSupportedGradleVersion("7.0")
    void isEmbeddedGradleDependency() {
        assertTrue(DependencyUtils.isEmbeddedGradleDependency(project.getDependencies().gradleApi()));
        assertTrue(DependencyUtils.isEmbeddedGradleDependency(project.getDependencies().gradleTestKit()));
        assertTrue(DependencyUtils.isEmbeddedGradleDependency(project.getDependencies().localGroovy()));
    }

    @Test
    @MinSupportedGradleVersion("7.0")
    void isEmbeddedGradleApiDependency() {
        assertTrue(DependencyUtils.isEmbeddedGradleApiDependency(project.getDependencies().gradleApi()));
        assertFalse(DependencyUtils.isEmbeddedGradleApiDependency(project.getDependencies().gradleTestKit()));
        assertFalse(DependencyUtils.isEmbeddedGradleApiDependency(project.getDependencies().localGroovy()));
    }

    @Test
    @MinSupportedGradleVersion("7.0")
    void isEmbeddedGradleTestKitDependency() {
        assertFalse(DependencyUtils.isEmbeddedGradleTestKitDependency(project.getDependencies().gradleApi()));
        assertTrue(DependencyUtils.isEmbeddedGradleTestKitDependency(project.getDependencies().gradleTestKit()));
        assertFalse(DependencyUtils.isEmbeddedGradleTestKitDependency(project.getDependencies().localGroovy()));
    }

    @Test
    @MinSupportedGradleVersion("7.0")
    void isEmbeddedLocalGroovyDependency() {
        assertFalse(DependencyUtils.isEmbeddedLocalGroovyDependency(project.getDependencies().gradleApi()));
        assertFalse(DependencyUtils.isEmbeddedLocalGroovyDependency(project.getDependencies().gradleTestKit()));
        assertTrue(DependencyUtils.isEmbeddedLocalGroovyDependency(project.getDependencies().localGroovy()));
    }

}
