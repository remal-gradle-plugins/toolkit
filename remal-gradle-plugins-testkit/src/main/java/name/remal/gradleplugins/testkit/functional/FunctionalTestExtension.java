package name.remal.gradleplugins.testkit.functional;

import static name.remal.gradleplugins.testkit.internal.containers.ProjectDirPrefix.getProjectDirPrefix;

import com.google.auto.service.AutoService;
import java.lang.reflect.Member;
import lombok.val;
import name.remal.gradleplugins.testkit.internal.containers.ExtensionStore;
import name.remal.gradleplugins.testkit.internal.containers.GradleProjectsContainer;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

@AutoService(Extension.class)
public class FunctionalTestExtension implements ParameterResolver, BeforeEachCallback {

    private final ExtensionStore extensionStore = new ExtensionStore(this);

    @Override
    public boolean supportsParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        val paramType = parameterContext.getParameter().getType();
        return paramType == GradleProject.class;
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        val gradleProjects = GradleProjectsContainer.getGradleProjectsContainer(extensionStore, extensionContext);
        return gradleProjects.resolveParameterGradleProject(parameterContext);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        val dirPrefix = getProjectDirPrefix(extensionStore, context);
        context.getTestClass().map(Class::getName).ifPresent(dirPrefix::push);
        context.getTestMethod().map(Member::getName).ifPresent(dirPrefix::push);
    }

}
