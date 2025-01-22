package name.remal.gradle_plugins.toolkit.testkit;

import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyProxy;
import static name.remal.gradle_plugins.toolkit.testkit.internal.containers.ProjectsContainer.getProjectsContainer;

import com.google.auto.service.AutoService;
import name.remal.gradle_plugins.toolkit.testkit.internal.AbstractProjectDirPrefixExtension;
import org.gradle.api.Project;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.invocation.Gradle;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

@AutoService(Extension.class)
public class GradleProjectExtension extends AbstractProjectDirPrefixExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        var paramType = parameterContext.getParameter().getType();
        return paramType == Project.class
            || paramType == Gradle.class;
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        var projects = getProjectsContainer(extensionStore, extensionContext);
        var project = projects.resolveParameterProject(parameterContext);

        var paramType = parameterContext.getParameter().getType();
        if (paramType == Gradle.class) {
            return asLazyProxy(GradleInternal.class, () -> (GradleInternal) project.getGradle());
        }

        return project;
    }

}
