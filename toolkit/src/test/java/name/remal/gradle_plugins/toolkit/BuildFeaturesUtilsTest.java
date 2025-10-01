package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.MaxTestableGradleVersion;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableGradleVersion;
import org.gradle.api.invocation.Gradle;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class BuildFeaturesUtilsTest {

    final Gradle gradle;


    @Test
    void isConfigurationCacheRequested() {
        assertDoesNotThrow(() ->
            BuildFeaturesUtils.isConfigurationCacheRequested(gradle)
        );
    }

    @Test
    @MinTestableGradleVersion("8.5")
    void isConfigurationCacheRequestedViaService() {
        assertDoesNotThrow(() ->
            BuildFeaturesUtils.isConfigurationCacheRequestedViaService(gradle)
        );
    }

    @Test
    @MaxTestableGradleVersion("8.4.9999")
    void isConfigurationCacheRequestedViaStartParameter() {
        assertDoesNotThrow(() ->
            BuildFeaturesUtils.isConfigurationCacheRequestedViaStartParameter(gradle)
        );
    }


    @Test
    void areIsolatedProjectsRequested() {
        assertDoesNotThrow(() ->
            BuildFeaturesUtils.areIsolatedProjectsRequested(gradle)
        );
    }

    @Test
    @MinTestableGradleVersion("8.5")
    void areIsolatedProjectsRequestedViaService() {
        assertDoesNotThrow(() ->
            BuildFeaturesUtils.areIsolatedProjectsRequestedViaService(gradle)
        );
    }

    @Test
    @MaxTestableGradleVersion("8.4.9999")
    @MinTestableGradleVersion("7.1")
    void areIsolatedProjectsRequestedViaStartParameter() {
        assertDoesNotThrow(() ->
            BuildFeaturesUtils.areIsolatedProjectsRequestedViaStartParameter(gradle)
        );
    }

}
