package name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CiUtils.isRunningOnCiIncludingTests;

import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;

@NoArgsConstructor(access = PRIVATE)
public abstract class MavenCentralRepositoryUtils {

    private static final String MAVEN_CENTRAL_MIRROR_NAME = "googleMavenCentralMirror";
    private static final String MAVEN_CENTRAL_MIRROR_URL = "https://maven-central.storage-download.googleapis.com/maven2/";


    /**
     * Emits a Maven Central repository declaration into the given build file {@code content}.
     *
     * <p>On a CI system the {@code googleMavenCentralMirror} is emitted first (see
     * {@link #addMavenCentralMirrorRepository}), followed by {@code repositories.mavenCentral()}.
     *
     * <p>The real Maven Central repository is always emitted too, because the mirror is not a full
     * mirror: it is synced on a regular basis, so very new artifacts can be missing from it.
     */
    public static void addMavenCentralRepository(JavaLikeContent<?> content) {
        if (isRunningOnCiIncludingTests()) {
            addMavenCentralMirrorRepository(content);
        }

        content.line("repositories.mavenCentral()");
    }

    /**
     * Emits the CI-only {@code googleMavenCentralMirror} repository declaration into the given build file
     * {@code content}.
     *
     * <p>This mirror is not a full mirror of Maven Central: it is synced on a regular basis, so very new
     * artifacts can be missing from it. Callers should also emit the real Maven Central repository as a
     * fallback.
     */
    public static void addMavenCentralMirrorRepository(JavaLikeContent<?> content) {
        content.block("repositories.maven", maven -> {
            maven.line("setName(\"%s\")", maven.escapeString(MAVEN_CENTRAL_MIRROR_NAME));
            maven.line("setUrl(\"%s\")", maven.escapeString(MAVEN_CENTRAL_MIRROR_URL));
            maven.line("mavenContent { releasesOnly() }");
        });
    }

}
