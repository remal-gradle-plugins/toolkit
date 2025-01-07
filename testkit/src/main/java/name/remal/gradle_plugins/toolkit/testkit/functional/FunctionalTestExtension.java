package name.remal.gradle_plugins.toolkit.testkit.functional;

import com.google.auto.service.AutoService;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.internal.AbstractProjectDirPrefixExtension;
import name.remal.gradle_plugins.toolkit.testkit.internal.containers.GradleProjectsContainer;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

@AutoService(Extension.class)
public class FunctionalTestExtension extends AbstractProjectDirPrefixExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        val paramType = parameterContext.getParameter().getType();
        return paramType == GradleProject.class
            || paramType == GradleKtsProject.class;
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        val gradleProjects = GradleProjectsContainer.getGradleProjectsContainer(extensionStore, extensionContext);
        return gradleProjects.resolveParameterGradleProject(parameterContext);
    }

}
