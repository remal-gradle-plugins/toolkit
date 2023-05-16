package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.internal.DefaultGradleRunner;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleRunnerUtils {

    @Contract("_,_->param1")
    @ReliesOnInternalGradleApi
    public static GradleRunner withJvmArguments(GradleRunner runner, List<String> jvmArguments) {
        ((DefaultGradleRunner) runner).withJvmArguments(jvmArguments);
        return runner;
    }

    @Contract("_,_->param1")
    public static GradleRunner withJvmArguments(GradleRunner runner, String... jvmArguments) {
        return withJvmArguments(runner, asList(jvmArguments));
    }

}
