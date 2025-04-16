package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeMethod;

import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolutionStrategy;

@NoArgsConstructor(access = PRIVATE)
@CustomLog
public abstract class ResolutionStrategyUtils {

    public static void configureGlobalResolutionStrategy(Project project, Action<ResolutionStrategy> action) {
        project.getConfigurations().configureEach(configuration -> {
            configuration.resolutionStrategy(action);
        });


        project.getPluginManager().withPlugin("io.spring.dependency-management", __ -> {
            Action<?> untypedAction = untypedResolutionStrategy -> {
                action.execute((ResolutionStrategy) untypedResolutionStrategy);
            };

            try {
                var dependencyManagement = project.getExtensions().getByName("dependencyManagement");
                invokeMethod(dependencyManagement, "resolutionStrategy", Action.class, untypedAction);

            } catch (Throwable e) {
                logger.warn("Error calling `dependencyManagement.resolutionStrategy()`", e);
            }
        });
    }

}
