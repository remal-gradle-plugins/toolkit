package name.remal.gradleplugins.toolkit;

import static java.lang.String.join;
import static java.lang.System.identityHashCode;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.val;
import name.remal.gradleplugins.toolkit.GradleUtilsBuildFinishedService.Params;
import org.gradle.api.Action;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

@SuppressWarnings("java:S5993")
abstract class GradleUtilsBuildFinishedService implements BuildService<Params>, AutoCloseable {

    private static final String SERVICE_NAME = join(
        "-",
        GradleUtilsBuildFinishedService.class.getName(),
        String.valueOf(identityHashCode(GradleUtilsBuildFinishedService.class))
    );

    public static void registerAction(Gradle gradle, Action<? super Gradle> action) {
        val buildFinishedService = gradle.getSharedServices().registerIfAbsent(
            SERVICE_NAME,
            GradleUtilsBuildFinishedService.class,
            service -> { }
        ).get();

        buildFinishedService.getParameters()
            .getActions()
            .addLast(() -> action.execute(gradle));
    }


    @Getter
    public static class Params implements BuildServiceParameters, Serializable {

        @Nullable
        private transient volatile Deque<Runnable> actions = new ConcurrentLinkedDeque<>();

        public synchronized Deque<Runnable> getActions() {
            if (actions == null) {
                actions = new ConcurrentLinkedDeque<>();
            }
            return requireNonNull(actions);
        }

    }


    public GradleUtilsBuildFinishedService() {
    }

    @Override
    public void close() {
        val actions = getParameters().getActions();
        while (true) {
            val action = actions.pollFirst();
            if (action == null) {
                break;
            }

            action.run();
        }
    }

}
