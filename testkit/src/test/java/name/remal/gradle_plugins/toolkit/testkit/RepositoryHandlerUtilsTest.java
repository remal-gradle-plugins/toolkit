package name.remal.gradle_plugins.toolkit.testkit;

import static name.remal.gradle_plugins.toolkit.CiUtils.isRunningOnCiIncludingTests;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class RepositoryHandlerUtilsTest {

    private final Project project;

    @Test
    void addMavenCentralMirrorRepository() {
        var repositories = project.getRepositories();
        assertThat(repositories)
            .as("repositories should start empty")
            .isEmpty();

        RepositoryHandlerUtils.addMavenCentralMirrorRepository(repositories);

        var mirror = repositories.findByName("googleMavenCentralMirror");
        assertThat(mirror)
            .as("googleMavenCentralMirror repository")
            .isInstanceOf(MavenArtifactRepository.class);
        assertThat(((MavenArtifactRepository) mirror).getUrl())
            .hasToString("https://maven-central.storage-download.googleapis.com/maven2/");
    }

    @Test
    void addMavenCentralRepository() {
        var repositories = project.getRepositories();
        assertThat(repositories)
            .as("repositories should start empty")
            .isEmpty();

        RepositoryHandlerUtils.addMavenCentralRepository(repositories);

        if (isRunningOnCiIncludingTests()) {
            assertThat(repositories)
                .as("mirror and Maven Central are registered on CI")
                .hasSize(2);
            assertThat(repositories.getNames())
                .as("mirror repository is registered on CI")
                .contains("googleMavenCentralMirror");
        } else {
            assertThat(repositories)
                .as("only Maven Central is registered when not on CI")
                .hasSize(1);
        }
    }

}
