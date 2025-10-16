package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.IsolatedActionUtils.toIsolatedAction;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.getMethod;

import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.DynamicCompatibilityCandidate;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleUtils {

    public static boolean isIncludedBuild(Gradle gradle) {
        return gradle.getParent() != null;
    }

    public static boolean isIncludedBuild(Project project) {
        return isIncludedBuild(project.getGradle());
    }


    private static final boolean ARE_SHARED_SERVICES_ENABLED = findMethod(Gradle.class, "getSharedServices") != null;

    public static void onGradleBuildFinished(Gradle gradle, Action<? super Gradle> action) {
        if (ARE_SHARED_SERVICES_ENABLED) {
            GradleUtilsBuildFinishedService.registerAction(gradle, action);

        } else {
            var buildFinishedMethod = getMethod(Gradle.class, "buildFinished", Action.class);
            buildFinishedMethod.invoke(gradle, __ -> action.execute(gradle));
        }
    }


    private static final boolean IS_GRADLE_LIFECYCLE_SUPPORTED = findMethod(Gradle.class, "getLifecycle") != null;

    @DynamicCompatibilityCandidate
    public static void beforeProjectWithLifecycleSupport(Gradle gradle, SerializableAction<? super Project> action) {
        if (IS_GRADLE_LIFECYCLE_SUPPORTED) {
            GradleLifecycleSupport.beforeProject(gradle, action);
        } else {
            gradle.beforeProject(action);
        }
    }

    @DynamicCompatibilityCandidate
    public static void afterProjectWithLifecycleSupport(Gradle gradle, SerializableAction<? super Project> action) {
        if (IS_GRADLE_LIFECYCLE_SUPPORTED) {
            GradleLifecycleSupport.afterProject(gradle, action);
        } else {
            gradle.afterProject(action);
        }
    }

    @MinCompatibleGradleVersion("8.8")
    private static class GradleLifecycleSupport {

        public static void beforeProject(Gradle gradle, SerializableAction<? super Project> action) {
            gradle.getLifecycle().beforeProject(toIsolatedAction(action));
        }

        public static void afterProject(Gradle gradle, SerializableAction<? super Project> action) {
            gradle.getLifecycle().afterProject(toIsolatedAction(action));
        }

    }

}
