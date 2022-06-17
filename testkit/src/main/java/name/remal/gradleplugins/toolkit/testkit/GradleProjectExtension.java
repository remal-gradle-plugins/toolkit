package name.remal.gradleplugins.toolkit.testkit;

import com.google.auto.service.AutoService;
import lombok.val;
import name.remal.gradleplugins.toolkit.testkit.internal.AbstractProjectDirPrefixExtension;
import name.remal.gradleplugins.toolkit.testkit.internal.containers.ProjectsContainer;
import org.gradle.api.Project;
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
        val paramType = parameterContext.getParameter().getType();
        return paramType == Project.class
            || paramType == Gradle.class;
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        val projects = ProjectsContainer.getProjectsContainer(extensionStore, extensionContext);
        val project = projects.resolveParameterProject(parameterContext);

        val paramType = parameterContext.getParameter().getType();
        if (paramType == Gradle.class) {
            return project.getGradle();
        }

        return project;
    }

}
