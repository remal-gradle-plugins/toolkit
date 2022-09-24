package name.remal.gradleplugins.toolkit;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class DependencyUtilsTest {

    private final Project project;

    @Test
    void platform() {
        val dependency = project.getDependencies().platform("test:test");
        assertThat(DependencyUtils.isPlatformDependency(dependency)).isTrue();
        assertThat(DependencyUtils.isEnforcedPlatformDependency(dependency)).isFalse();
        assertThat(DependencyUtils.isDocumentationDependency(dependency)).isFalse();
    }

    @Test
    void enforcedPlatform() {
        val dependency = project.getDependencies().enforcedPlatform("test:test");
        assertThat(DependencyUtils.isPlatformDependency(dependency)).isTrue();
        assertThat(DependencyUtils.isEnforcedPlatformDependency(dependency)).isTrue();
        assertThat(DependencyUtils.isDocumentationDependency(dependency)).isFalse();
    }

}
