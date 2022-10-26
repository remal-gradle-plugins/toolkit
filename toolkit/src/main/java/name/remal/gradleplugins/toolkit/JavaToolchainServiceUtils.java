package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradleplugins.toolkit.ServiceRegistryUtils.getService;

import lombok.NoArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.util.GradleVersion;

@NoArgsConstructor(access = PRIVATE)
public abstract class JavaToolchainServiceUtils {

    private static final GradleVersion MIN_SERVICE_VERSION = GradleVersion.version("7.0");

    public static JavaToolchainService getJavaToolchainServiceFor(Project project) {
        try {
            return getService(project, JavaToolchainService.class);

        } catch (Exception exception) {
            if (GradleVersion.current().compareTo(MIN_SERVICE_VERSION) < 0) {
                try {
                    project.getPluginManager().apply(JavaBasePlugin.class);
                    return getExtension(project, JavaToolchainService.class);
                } catch (Exception e) {
                    exception.addSuppressed(e);
                }
            }

            throw exception;
        }
    }

}
