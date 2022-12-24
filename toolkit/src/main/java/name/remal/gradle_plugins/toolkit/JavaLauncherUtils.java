package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.JavaToolchainServiceUtils.getJavaToolchainToolProviderFor;

import lombok.NoArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;

@MinGradleVersion("6.7")
@NoArgsConstructor(access = PRIVATE)
public abstract class JavaLauncherUtils {

    public static Provider<JavaLauncher> getJavaLauncherProviderFor(Project project) {
        return getJavaToolchainToolProviderFor(project, JavaToolchainService::launcherFor);
    }

}
