package name.remal.gradleplugins.testkit.internal.containers;

import static java.lang.String.format;
import static java.nio.file.Files.createTempDirectory;
import static org.codehaus.groovy.runtime.ResourceGroovyMethods.deleteDir;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradleplugins.testkit.ChildProjectOf;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectStateInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

public class ProjectsContainer extends AbstractExtensionContextContainer<Project> {

    public static ProjectsContainer getProjectsContainer(ExtensionStore extensionStore, ExtensionContext context) {
        ProjectsContainer projectsContainer = extensionStore.getCurrentStoreValue(context, ProjectsContainer.class);
        if (projectsContainer != null) {
            return projectsContainer;
        }

        return extensionStore.setCurrentStoreValue(context, new ProjectsContainer(extensionStore, context));
    }


    public ProjectsContainer(ExtensionStore extensionStore, ExtensionContext context) {
        super(extensionStore, context);
    }

    @Override
    protected void cleanup(Project project, boolean isExceptionThrown) {
        if (!isExceptionThrown) {
            val projectDir = project.getProjectDir();
            deleteDir(projectDir);
        }
    }


    public Project newProject(@Nullable Project parentProject) {
        val dirPrefix = getDirPrefix();
        return newProject(parentProject, dirPrefix);
    }

    @SneakyThrows
    private synchronized Project newProject(@Nullable Project parentProject, ProjectDirPrefix dirPrefix) {
        final Project project;
        if (parentProject == null) {
            val projectDir = createTempDirectory(dirPrefix.toString()).toAbsolutePath().toFile();
            project = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName(projectDir.getName())
                .build();

        } else {
            project = ProjectBuilder.builder()
                .withParent(parentProject)
                .build();
        }

        val stateInternal = (ProjectStateInternal) project.getState();
        stateInternal.toBeforeEvaluate();

        registerResource(project);

        return project;
    }


    public Project resolveParameterProject(ParameterContext parameterContext, ExtensionContext extensionContext) {
        val annotatedParam = new AnnotatedParam(parameterContext.getParameter());
        return resolveParameterProject(annotatedParam, extensionContext);
    }

    private final Map<AnnotatedParam, Project> parameterProjects = new LinkedHashMap<>();

    public synchronized Project resolveParameterProject(
        AnnotatedParam annotatedParam,
        ExtensionContext extensionContext
    ) {
        Project paramProject = parameterProjects.get(annotatedParam);
        if (paramProject != null) {
            return paramProject;
        }

        val dirPrefix = getDirPrefix()
            .newChildPrefix()
            .push(annotatedParam.getName());

        val childProjectOf = annotatedParam.findAnnotation(ChildProjectOf.class);
        if (childProjectOf != null) {
            val parentProjectParamName = childProjectOf.value();
            if (annotatedParam.getName().equals(parentProjectParamName)) {
                throw new IllegalStateException(format(
                    "%s is annotated with @%s that references to itself",
                    annotatedParam,
                    ChildProjectOf.class.getSimpleName()
                ));
            }
            for (val otherParam : annotatedParam.getDeclaringExecutable().getParameters()) {
                val otherAnnotatedParam = new AnnotatedParam(otherParam);
                if (otherParam.getName().equals(parentProjectParamName)) {
                    val parentProject = resolveParameterProject(otherAnnotatedParam, extensionContext);
                    paramProject = newProject(parentProject, dirPrefix);
                    break;
                }
            }
            if (paramProject == null) {
                throw new IllegalStateException(format(
                    "Executable %s doesn't have a parameter with name '%s', which is required due to @%s annotation",
                    annotatedParam.getDeclaringExecutable(),
                    parentProjectParamName,
                    ChildProjectOf.class.getSimpleName()
                ));
            }

        } else {
            paramProject = newProject(null, dirPrefix);
        }

        parameterProjects.put(annotatedParam, paramProject);
        return paramProject;
    }

}
