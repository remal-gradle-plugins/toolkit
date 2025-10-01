package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.GradleVersionUtils.isCurrentGradleVersionGreaterThanOrEqualTo;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.getMethod;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeMethod;

import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.DynamicCompatibilityCandidate;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.StartParameter;
import org.gradle.api.configuration.BuildFeatures;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.StartParameterInternal;
import org.gradle.api.invocation.Gradle;
import org.jetbrains.annotations.VisibleForTesting;

@NoArgsConstructor(access = PRIVATE)
@DynamicCompatibilityCandidate
@ReliesOnInternalGradleApi
public abstract class BuildFeaturesUtils {

    private static final boolean IS_BUILD_FEATURES_SERVICE_AVAILABLE =
        isCurrentGradleVersionGreaterThanOrEqualTo("8.5");

    private static final boolean ARE_ISOLATED_PROJECTS_AVAILABLE =
        isCurrentGradleVersionGreaterThanOrEqualTo("7.1");


    public static boolean isConfigurationCacheRequested(Gradle gradle) {
        if (IS_BUILD_FEATURES_SERVICE_AVAILABLE) {
            return isConfigurationCacheRequestedViaService(gradle);
        }

        return isConfigurationCacheRequestedViaStartParameter(gradle);
    }

    @MinCompatibleGradleVersion("8.5")
    @VisibleForTesting
    static boolean isConfigurationCacheRequestedViaService(Gradle gradle) {
        var gradleInternal = (GradleInternal) gradle;
        var buildFeatures = gradleInternal.getServices().get(BuildFeatures.class);
        return buildFeatures.getConfigurationCache().getRequested().getOrElse(false);
    }

    @VisibleForTesting
    static boolean isConfigurationCacheRequestedViaStartParameter(Gradle gradle) {
        var startParameter = gradle.getStartParameter();
        var isConfigurationCacheRequested =
            findMethod(StartParameter.class, boolean.class, "isConfigurationCacheRequested");
        if (isConfigurationCacheRequested != null) {
            return isConfigurationCacheRequested.invoke(startParameter);
        }

        var startParameterInternal = (StartParameterInternal) startParameter;
        var getConfigurationCache = findMethod(StartParameterInternal.class, Object.class, "getConfigurationCache");
        if (getConfigurationCache != null) {
            var optionValue = getConfigurationCache.invoke(startParameterInternal);
            return (Boolean) invokeMethod(optionValue, Object.class, "get");
        }

        var isConfigurationCache =
            getMethod(StartParameterInternal.class, boolean.class, "isConfigurationCache");
        return isConfigurationCache.invoke(startParameterInternal);
    }


    public static boolean areIsolatedProjectsRequested(Gradle gradle) {
        if (!ARE_ISOLATED_PROJECTS_AVAILABLE) {
            return false;
        }

        if (IS_BUILD_FEATURES_SERVICE_AVAILABLE) {
            return areIsolatedProjectsRequestedViaService(gradle);
        }

        return areIsolatedProjectsRequestedViaStartParameter(gradle);
    }

    @MinCompatibleGradleVersion("8.5")
    @VisibleForTesting
    static boolean areIsolatedProjectsRequestedViaService(Gradle gradle) {
        var gradleInternal = (GradleInternal) gradle;
        var buildFeatures = gradleInternal.getServices().get(BuildFeatures.class);
        return buildFeatures.getIsolatedProjects().getRequested().getOrElse(false);
    }

    @MinCompatibleGradleVersion("7.1")
    @VisibleForTesting
    static boolean areIsolatedProjectsRequestedViaStartParameter(Gradle gradle) {
        var startParameter = gradle.getStartParameter();
        var startParameterInternal = (StartParameterInternal) startParameter;
        var getIsolatedProjects = getMethod(StartParameterInternal.class, Object.class, "getIsolatedProjects");
        var optionValue = getIsolatedProjects.invoke(startParameterInternal);
        return (Boolean) invokeMethod(optionValue, Object.class, "get");
    }

}
