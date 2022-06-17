package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.findMethod;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.getMethod;

import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.invocation.Gradle;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleUtils {

    private static final boolean ARE_SHARED_SERVICES_ENABLED = findMethod(Gradle.class, "getSharedServices") != null;

    public static void onGradleBuildFinished(Gradle gradle, Action<? super Gradle> action) {
        if (ARE_SHARED_SERVICES_ENABLED) {
            GradleUtilsBuildFinishedService.registerAction(gradle, action);

        } else {
            val buildFinishedMethod = getMethod(Gradle.class, "buildFinished", Action.class);
            buildFinishedMethod.invoke(gradle, __ -> action.execute(gradle));
        }
    }

}
