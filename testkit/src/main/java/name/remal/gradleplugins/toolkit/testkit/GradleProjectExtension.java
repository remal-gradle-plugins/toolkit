package name.remal.gradleplugins.toolkit.testkit;

import com.google.auto.service.AutoService;
import lombok.val;
import name.remal.gradleplugins.toolkit.testkit.internal.AbstractProjectDirPrefixExtension;
import name.remal.gradleplugins.toolkit.testkit.internal.containers.ProjectsContainer;
import org.gradle.api.Project;
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
        return paramType == Project.class;
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        val projects = ProjectsContainer.getProjectsContainer(extensionStore, extensionContext);
        return projects.resolveParameterProject(parameterContext);
    }

}
