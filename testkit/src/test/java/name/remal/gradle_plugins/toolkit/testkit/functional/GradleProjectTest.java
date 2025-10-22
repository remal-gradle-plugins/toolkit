package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.nio.file.Files.writeString;
import static name.remal.gradle_plugins.toolkit.ArchiveUtils.newEmptyZipArchive;
import static name.remal.gradle_plugins.toolkit.InTestFlags.IS_IN_FUNCTIONAL_TEST_ENV_VAR;
import static name.remal.gradle_plugins.toolkit.InTestFlags.IS_IN_TEST_ENV_VAR;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static name.remal.gradle_plugins.toolkit.StringUtils.normalizeString;
import static org.apache.commons.lang3.StringUtils.join;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GradleProjectTest extends GradleProjectTestBase<GradleProject> {

    GradleProjectTest(GradleProject project) {
        super(project);
    }


    @Nested
    class EnvironmentVariables {

        @Test
        void defaultEnvironmentVariables() {
            project.withoutConfigurationCache();

            check("System.getenv('" + IS_IN_TEST_ENV_VAR + "') == 'true'");
            check("System.getenv('" + IS_IN_FUNCTIONAL_TEST_ENV_VAR + "') == 'true'");

            // to make sure `additionalEnvironmentVariables` test works correctly:
            check("System.getenv('_TEST_ENV_VAR_') == null");

            project.assertBuildSuccessfully("help");
        }

        @Test
        void additionalEnvironmentVariables() {
            project.withoutConfigurationCache();

            project.putEnvironmentVariable("_TEST_ENV_VAR_", true);

            check("System.getenv('" + IS_IN_TEST_ENV_VAR + "') == 'true'");
            check("System.getenv('" + IS_IN_FUNCTIONAL_TEST_ENV_VAR + "') == 'true'");

            check("System.getenv('_TEST_ENV_VAR_') == 'true'");

            project.assertBuildSuccessfully("help");
        }

    }

    @Test
    void baseRunnerProperties() {
        project.withoutConfigurationCache();

        check("gradle.startParameter.showStacktrace?.toString() == 'ALWAYS'");
        check("gradle.startParameter.warningMode?.toString() == 'All'");
        check("gradle.startParameter.parallelProjectExecutionEnabled");
        check("gradle.startParameter.maxWorkerCount >= 2");

        check("project.findProperty('org.gradle.daemon') == null");
        check("project.findProperty('org.gradle.daemon.idletimeout')?.toString() == '5000'");

        project.assertBuildSuccessfully("help");
    }

    @Test
    void watchFileSystemMode() {
        project.withoutConfigurationCache();

        check("gradle.startParameter.watchFileSystemMode?.toString() == 'DISABLED'");

        project.assertBuildSuccessfully("help");
    }

    @Test
    void mavenLocalRepoIsRedefined() throws Exception {
        project.withoutConfigurationCache();

        writeString(
            createParentDirectories(project.getMavenLocalRepoDir().toPath().resolve(
                "test/test/test/test-test.pom"
            )),
            join("\n",
                "<project>",
                "  <modelVersion>4.0.0</modelVersion>",
                "  <groupId>test</groupId>",
                "  <artifactId>test</artifactId>",
                "  <version>test</version>",
                "</project>"
            )
        );

        newEmptyZipArchive(new File(project.getMavenLocalRepoDir(), "test/test/test/test-test.jar"));

        project.forBuildFile(build -> {
            build.applyPlugin("java");
            build.line("repositories { mavenLocal() }");
            build.line("dependencies { implementation 'test:test:test' }");
            build.line("configurations.compileClasspath.files");
        });

        project.assertBuildSuccessfully("help");
    }

    @Nested
    class DependencyVerification {

        @BeforeEach
        void beforeEach() throws Exception {
            project.withoutConfigurationCache();

            writeString(
                createParentDirectories(project.getMavenLocalRepoDir().toPath().resolve(
                    "test/test/test/test-test.pom"
                )),
                join("\n",
                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">",
                    "  <modelVersion>4.0.0</modelVersion>",
                    "  <groupId>test</groupId>",
                    "  <artifactId>test</artifactId>",
                    "  <version>test</version>",
                    "</project>"
                )
            );

            newEmptyZipArchive(new File(project.getMavenLocalRepoDir(), "test/test/test/test-test.jar"));


            writeString(
                createParentDirectories(project.getProjectDir().toPath().resolve(
                    "gradle/verification-metadata.xml"
                )),
                join("\n",
                    "<verification-metadata xmlns=\"https://schemas.gradle.org/dependency-verification/1.0\">",
                    "  <configuration>",
                    "    <verify-metadata>false</verify-metadata>",
                    "    <components>",
                    "      <component group=\"test\" name=\"test\" version=\"test\">",
                    "        <artifact name=\"test-test.jar\">",
                    "          <sha256 value=\"0000000000000000000000000000000000000000000000000000000000000000\"/>",
                    "        </artifact>",
                    "      </component>",
                    "    </components>",
                    "    <trusted-artifacts>",
                    "      <trust group=\"org.gradle.toolchains.foojay-resolver-convention\" reason=\"test infra\"/>",
                    "      <trust group=\"org.gradle.toolchains.foojay-resolver\" reason=\"test infra\"/>",
                    "      <trust group=\"org.gradle.toolchains\" reason=\"test infra\"/>",
                    "      <trust group=\"com.google.code.gson\" reason=\"test infra\"/>",
                    "    </trusted-artifacts>",
                    "  </configuration>",
                    "</verification-metadata>"
                )
            );


            project.forBuildFile(build -> {
                build.applyPlugin("java");
                build.line("repositories { mavenLocal() }");
                build.line("dependencies { implementation 'test:test:test' }");
                build.line("configurations.compileClasspath.files");
            });
        }

        @Test
        void defaultSettings() {
            var buildResult = project.assertBuildFails("help");
            var normalizedOutput = normalizeString(buildResult.getOutput());

            assertThat(normalizedOutput)
                .contains("On artifact test-test.jar (test:test:test) in repository 'MavenLocal'"
                    + ": expected a 'sha256' checksum"
                    + " of '0000000000000000000000000000000000000000000000000000000000000000'"
                );
        }

        @Test
        void disabled() {
            project.putGradleProperty("org.gradle.dependency.verification", "off");
            project.assertBuildSuccessfully("help");
        }

    }

}
