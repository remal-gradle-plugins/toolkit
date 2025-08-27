package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.MethodsInvoker.invokeMethod;

import lombok.NoArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = PRIVATE)
public abstract class ResolutionStrategyUtils {

    private static final Logger logger = LoggerFactory.getLogger(ResolutionStrategyUtils.class);

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
