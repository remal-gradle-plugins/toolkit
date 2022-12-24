package name.remal.gradle_plugins.toolkit;

import java.io.File;
import org.gradle.api.provider.Provider;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;

interface JacocoPluginExtensionUtilsMethods {

    File getReportsDir(JacocoPluginExtension extension);

    void setReportsDir(JacocoPluginExtension extension, Provider<File> reportsDirProvider);

    void setReportsDir(JacocoPluginExtension extension, File reportsDir);

}
