package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.lang.String.format;
import static lombok.AccessLevel.PROTECTED;
import static name.remal.gradle_plugins.toolkit.StringUtils.normalizeString;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks.PluginManagementChunk;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor(access = PROTECTED)
abstract class GradleProjectTestBase<GradleProjectType extends AbstractGradleProject<?, ?, ?, ?>> {

    protected final GradleProjectType project;

    protected final String escapeString(Object value) {
        return project.getBuildFile().escapeString(value.toString());
    }


    @BeforeEach
    @OverridingMethodsMustInvokeSuper
    protected void beforeEach() {
        project.withoutPluginClasspath();
    }


    protected final void check(String condition) {
        if (project instanceof GradleProject) {
            project.getBuildFile().line("assert " + condition);

        } else if (project instanceof GradleKtsProject) {
            project.getBuildFile().block("if (!(" + condition + "))", block -> {
                block.line("throw AssertionError(\"Assertion failed: %s\")", block.escapeString(condition));
            });

        } else {
            throw new UnsupportedOperationException();
        }
    }

    @FormatMethod
    protected final void check(@FormatString String conditionFormat, @Nullable Object... conditionArgs) {
        check(format(conditionFormat, conditionArgs));
    }

    @Test
    void checkThrowsException() {
        project.withoutConfigurationCache();
        check("false");
        var buildResult = project.assertBuildFails("help");
        var normalizedOutput = normalizeString(buildResult.getOutput());

        if (project instanceof GradleProject) {
            assertThat(normalizedOutput)
                .contains("\nCaused by: Assertion failed:\n\nassert false\n");

        } else if (project instanceof GradleKtsProject) {
            assertThat(normalizedOutput)
                .contains("\nAssertion failed: false\n");

        } else {
            throw new UnsupportedOperationException();
        }
    }


    @Test
    void pluginManagementWithMavenCentralMirror() {
        project.forSettingsFile(settings -> {
            settings.getChunk(PluginManagementChunk.class).setWithMavenCentralMirror(true);
            settings.setWithFoojayToolchainsResolver(false);
        });

        assertThat(project.getSettingsFile().toString())
            .contains(escapeString("https://maven-central.storage-download.googleapis.com/maven2/"));

        project.assertBuildSuccessfully("help");
    }

    @Test
    void pluginManagementWithoutMavenCentralMirror() {
        project.forSettingsFile(settings -> {
            settings.getChunk(PluginManagementChunk.class).setWithMavenCentralMirror(false);
            settings.setWithFoojayToolchainsResolver(false);
        });

        assertThat(project.getSettingsFile().toString())
            .doesNotContain(escapeString("https://maven-central.storage-download.googleapis.com/maven2/"));

        project.assertBuildSuccessfully("help");
    }

}
