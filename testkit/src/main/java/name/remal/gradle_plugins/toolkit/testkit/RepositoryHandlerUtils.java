package name.remal.gradle_plugins.toolkit.testkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CiUtils.isRunningOnCiIncludingTests;

import lombok.NoArgsConstructor;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;

@NoArgsConstructor(access = PRIVATE)
public abstract class RepositoryHandlerUtils {

    private static final String MAVEN_CENTRAL_MIRROR_NAME = "googleMavenCentralMirror";
    private static final String MAVEN_CENTRAL_MIRROR_URL = "https://maven-central.storage-download.googleapis.com/maven2/";


    /**
     * Registers Maven Central on the given {@link RepositoryHandler}.
     *
     * <p>On a CI system the {@code googleMavenCentralMirror} is registered first (see
     * {@link #addMavenCentralMirrorRepository}), followed by the real Maven Central repository.
     *
     * <p>The real Maven Central repository is always registered too, because the mirror is not a full
     * mirror: it is synced on a regular basis, so very new artifacts can be missing from it.
     */
    public static void addMavenCentralRepository(RepositoryHandler repositories) {
        if (isRunningOnCiIncludingTests()) {
            addMavenCentralMirrorRepository(repositories);
        }

        repositories.mavenCentral();
    }

    /**
     * Registers the CI-only {@code googleMavenCentralMirror} repository on the given
     * {@link RepositoryHandler}.
     *
     * <p>This mirror is not a full mirror of Maven Central: it is synced on a regular basis, so very new
     * artifacts can be missing from it. Callers should register the real Maven Central repository as a
     * fallback.
     */
    public static void addMavenCentralMirrorRepository(RepositoryHandler repositories) {
        repositories.maven(repo -> {
            repo.setName(MAVEN_CENTRAL_MIRROR_NAME);
            repo.setUrl(MAVEN_CENTRAL_MIRROR_URL);
            repo.mavenContent(MavenRepositoryContentDescriptor::releasesOnly);
        });
    }

}
