package name.remal.gradleplugins.toolkit;

import com.google.auto.service.AutoService;
import java.io.File;
import org.gradle.api.provider.Provider;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;

@AutoService(JacocoPluginExtensionUtilsMethods.class)
final class JacocoPluginExtensionUtilsMethods_6_7 implements JacocoPluginExtensionUtilsMethods {

    @Override
    public File getReportsDir(JacocoPluginExtension extension) {
        return extension.getReportsDir();
    }

    @Override
    public void setReportsDir(JacocoPluginExtension extension, Provider<File> reportsDirProvider) {
        extension.setReportsDir(reportsDirProvider);
    }

    @Override
    public void setReportsDir(JacocoPluginExtension extension, File reportsDir) {
        extension.setReportsDir(reportsDir);
    }

}
