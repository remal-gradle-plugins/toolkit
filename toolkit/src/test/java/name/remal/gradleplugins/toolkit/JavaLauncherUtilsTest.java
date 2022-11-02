package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.JavaInstallationMetadataUtils.getJavaInstallationVersionOf;
import static name.remal.gradleplugins.toolkit.JavaLauncherUtils.getJavaLauncherFor;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class JavaLauncherUtilsTest {

    private final Project project;

    @Nested
    class GetJavaLauncherFor {

        @Test
        void currentJvm() {
            val javaLauncher = getJavaLauncherFor(project);
            val javaVersion = getJavaInstallationVersionOf(javaLauncher.getMetadata());
            assertThat(javaVersion).isEqualTo(JavaVersion.current());
        }

    }

}
