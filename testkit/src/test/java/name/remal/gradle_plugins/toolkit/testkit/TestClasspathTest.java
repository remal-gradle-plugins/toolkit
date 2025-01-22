package name.remal.gradle_plugins.toolkit.testkit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestClasspathTest {

    @Test
    void junitJupiterApi_version() {
        var version = TestClasspath.getTestClasspathLibraryVersion(
            "org.junit.jupiter:junit-jupiter-api"
        );
        assertThat(version)
            .isNotEmpty()
            .matches("\\d+\\..+");
    }

    @Test
    void junitJupiterEngine_version() {
        var version = TestClasspath.getTestClasspathLibraryVersion(
            "org.junit.jupiter:junit-jupiter-engine"
        );
        assertThat(version)
            .isNotEmpty()
            .matches("\\d+\\..+");
    }


    @Test
    void junitJupiterApi_full_notation() {
        var fullNotation = TestClasspath.getTestClasspathLibraryFullNotation(
            "org.junit.jupiter:junit-jupiter-api"
        );
        var version = TestClasspath.getTestClasspathLibraryVersion(
            "org.junit.jupiter:junit-jupiter-api"
        );
        assertThat(fullNotation)
            .isNotEmpty()
            .isEqualTo("org.junit.jupiter:junit-jupiter-api:" + version);
    }

    @Test
    void junitJupiterEngine_full_notation() {
        var fullNotation = TestClasspath.getTestClasspathLibraryFullNotation(
            "org.junit.jupiter:junit-jupiter-engine"
        );
        var version = TestClasspath.getTestClasspathLibraryVersion(
            "org.junit.jupiter:junit-jupiter-engine"
        );
        assertThat(fullNotation)
            .isNotEmpty()
            .isEqualTo("org.junit.jupiter:junit-jupiter-engine:" + version);
    }


    @Test
    void junitJupiterApi_file_paths() {
        var filePaths = TestClasspath.getTestClasspathLibraryFilePaths(
            "org.junit.jupiter:junit-jupiter-api"
        );
        assertThat(filePaths)
            .isNotEmpty();
    }

    @Test
    void junitJupiterEngine_file_paths() {
        var filePaths = TestClasspath.getTestClasspathLibraryFilePaths(
            "org.junit.jupiter:junit-jupiter-engine"
        );
        assertThat(filePaths)
            .isNotEmpty();
    }

}
