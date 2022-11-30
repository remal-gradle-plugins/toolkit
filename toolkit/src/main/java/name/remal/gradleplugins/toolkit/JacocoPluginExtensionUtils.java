package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.CrossCompileServices.loadCrossCompileService;

import java.io.File;
import lombok.NoArgsConstructor;
import org.gradle.api.provider.Provider;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;

@NoArgsConstructor(access = PRIVATE)
public abstract class JacocoPluginExtensionUtils {

    private static final JacocoPluginExtensionUtilsMethods METHODS =
        loadCrossCompileService(JacocoPluginExtensionUtilsMethods.class);

    public static File getReportsDir(JacocoPluginExtension extension) {
        return METHODS.getReportsDir(extension);
    }

    public static void setReportsDir(JacocoPluginExtension extension, Provider<File> reportsDirProvider) {
        METHODS.setReportsDir(extension, reportsDirProvider);
    }

    public static void setReportsDir(JacocoPluginExtension extension, File reportsDir) {
        METHODS.setReportsDir(extension, reportsDir);
    }

}
