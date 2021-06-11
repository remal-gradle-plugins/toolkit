package name.remal.gradleplugins.toolkit.testkit.internal.containers;

import static java.nio.file.Files.createTempDirectory;

import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradleplugins.toolkit.testkit.functional.GradleProject;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

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
        val projectDir = createTempDirectory(dirPrefix.toString()).toAbsolutePath().toFile();
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
