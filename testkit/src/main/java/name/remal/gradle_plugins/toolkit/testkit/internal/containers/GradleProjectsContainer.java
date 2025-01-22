package name.remal.gradle_plugins.toolkit.testkit.internal.containers;

import name.remal.gradle_plugins.toolkit.testkit.functional.AbstractGradleProject;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleKtsProject;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

@Internal
public class GradleProjectsContainer extends AbstractExtensionContextContainer<AbstractGradleProject<?, ?, ?, ?>> {

    public static GradleProjectsContainer getGradleProjectsContainer(
        ExtensionStore extensionStore,
        ExtensionContext context
    ) {
        GradleProjectsContainer projectsContainer = extensionStore.getCurrentStoreValue(
            context,
            GradleProjectsContainer.class
        );
        if (projectsContainer != null) {
            return projectsContainer;
        }

        return extensionStore.setCurrentStoreValue(context, new GradleProjectsContainer(extensionStore, context));
    }


    public GradleProjectsContainer(ExtensionStore extensionStore, ExtensionContext context) {
        super(extensionStore, context);
    }

    public AbstractGradleProject<?, ?, ?, ?> resolveParameterGradleProject(ParameterContext parameterContext) {
        var annotatedParam = new AnnotatedParam(parameterContext.getParameter());
        var parameterType = parameterContext.getParameter().getType();

        var dirPrefix = getDirPrefix()
            .newChildPrefix()
            .push(annotatedParam.getName());
        var projectDir = dirPrefix.createTempDir().toFile();

        final AbstractGradleProject<?, ?, ?, ?> gradleProject;
        if (parameterType == GradleProject.class) {
            gradleProject = new GradleProject(projectDir);
        } else if (parameterType == GradleKtsProject.class) {
            gradleProject = new GradleKtsProject(projectDir);
        } else {
            throw new IllegalStateException("Unsupported parameter type: " + parameterType);
        }

        registerResource(gradleProject);
        return gradleProject;
    }

}
