package name.remal.gradle_plugins.toolkit.testkit.internal.containers;

import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.testkit.functional.GradleProject;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

@Internal
public class GradleProjectsContainer extends AbstractExtensionContextContainer<GradleProject> {

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

    public GradleProject newGradleProject() {
        val dirPrefix = getDirPrefix();
        return newGradleProject(dirPrefix);
    }

    @SneakyThrows
    private GradleProject newGradleProject(ProjectDirPrefix dirPrefix) {
        val projectDir = dirPrefix.createTempDir().toFile();
        val gradleProject = new GradleProject(projectDir);
        registerResource(gradleProject);
        return gradleProject;
    }


    public GradleProject resolveParameterGradleProject(ParameterContext parameterContext) {
        val annotatedParam = new AnnotatedParam(parameterContext.getParameter());
        val dirPrefix = getDirPrefix()
            .newChildPrefix()
            .push(annotatedParam.getName());
        return newGradleProject(dirPrefix);
    }

}
