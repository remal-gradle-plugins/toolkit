package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.JacocoPluginExtensionUtils.getReportsDir;
import static name.remal.gradle_plugins.toolkit.JacocoPluginExtensionUtils.setReportsDir;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.junit.jupiter.api.Test;

class JacocoPluginExtensionUtilsTest {

    private final Project project;
    private final JacocoPluginExtension extension;

    public JacocoPluginExtensionUtilsTest(Project project) {
        this.project = project;

        project.getPluginManager().apply("jacoco");
        this.extension = getExtension(project, JacocoPluginExtension.class);
    }

    @Test
    void setReportsDirProvider() {
        val reportsDir = normalizeFile(new File("/reports"));
        assertThat(getReportsDir(extension))
            .isNotNull()
            .isNotEqualTo(reportsDir);

        setReportsDir(extension, project.provider(() -> reportsDir));
        assertThat(getReportsDir(extension))
            .isEqualTo(reportsDir);
    }

    @Test
    void setReportsDirFile() {
        val reportsDir = normalizeFile(new File("/reports"));
        assertThat(getReportsDir(extension))
            .isNotNull()
            .isNotEqualTo(reportsDir);

        setReportsDir(extension, reportsDir);
        assertThat(getReportsDir(extension))
            .isEqualTo(reportsDir);
    }

}
