package name.remal.gradle_plugins.toolkit.testkit.functional.generator.utils;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CiUtils.isRunningOnCiIncludingTests;

import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;

@NoArgsConstructor(access = PRIVATE)
public abstract class MavenCentralRepositoryUtils {

    private static final String MAVEN_CENTRAL_MIRROR_NAME = "googleMavenCentralMirror";
    private static final String MAVEN_CENTRAL_MIRROR_URL = "https://maven-central.storage-download.googleapis.com/maven2/";


    public static void addMavenCentralRepository(JavaLikeContent<?> content) {
        if (isRunningOnCiIncludingTests()) {
            addMavenCentralMirrorRepository(content);
        }

        content.line("repositories.mavenCentral()");
    }

    public static void addMavenCentralMirrorRepository(JavaLikeContent<?> content) {
        content.block("repositories.maven", maven -> {
            maven.line("setName(\"%s\")", maven.escapeString(MAVEN_CENTRAL_MIRROR_NAME));
            maven.line("setUrl(\"%s\")", maven.escapeString(MAVEN_CENTRAL_MIRROR_URL));
            maven.line("mavenContent { releasesOnly() }");
        });
    }

}
