package name.remal.gradle_plugins.toolkit.testkit.internal.containers;

import static java.lang.String.format;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyProxy;
import static org.codehaus.groovy.runtime.ResourceGroovyMethods.deleteDir;

import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import name.remal.gradle_plugins.toolkit.testkit.ApplyPlugin;
import name.remal.gradle_plugins.toolkit.testkit.ChildProjectOf;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.ProjectStateInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

@Internal
public class ProjectsContainer extends AbstractExtensionContextContainer<Project> {

    public static ProjectsContainer getProjectsContainer(ExtensionStore extensionStore, ExtensionContext context) {
        ProjectsContainer projectsContainer = extensionStore.getCurrentStoreValue(context, ProjectsContainer.class);
        if (projectsContainer != null) {
            return projectsContainer;
        }

        return extensionStore.setCurrentStoreValue(context, new ProjectsContainer(extensionStore, context));
    }


    private final Map<Parameter, Long> invocationNumbers = new LinkedHashMap<>();
    private final Map<Long, Map<AnnotatedParam, Project>> invocationParameterProjects = new LinkedHashMap<>();

    public ProjectsContainer(ExtensionStore extensionStore, ExtensionContext context) {
        super(extensionStore, context);
    }

    @Override
    protected void cleanup(Project project, boolean isExceptionThrown) {
        if (!isExceptionThrown) {
            var projectDir = project.getProjectDir();
            deleteDir(projectDir);
        }
    }

    @Override
    protected void additionalCleanup(boolean isExceptionThrown) {
        invocationNumbers.clear();
        invocationParameterProjects.clear();
    }

    @ReliesOnInternalGradleApi
    @SneakyThrows
    private synchronized Project newProject(@Nullable Project parentProject, ProjectDirPrefix dirPrefix) {
        final Supplier<Project> projectCreator;
        if (parentProject == null) {
            projectCreator = () -> {
                var projectDir = dirPrefix.createTempDir().toFile();
                return ProjectBuilder.builder()
                    .withProjectDir(projectDir)
                    .withName(projectDir.getName())
                    .build();
            };

        } else {
            projectCreator = () -> {
                return ProjectBuilder.builder()
                    .withParent(parentProject)
                    .build();
            };
        }

        return asLazyProxy(ProjectInternal.class, () -> {
            var project = projectCreator.get();
            registerResource(project);
            evaluateProjectIfNeeded(project);
            return (ProjectInternal) project;
        });
    }

    private static void evaluateProjectIfNeeded(Project project) {
        var stateInternal = (ProjectStateInternal) project.getState();
        if (stateInternal.isUnconfigured()) {
            stateInternal.toBeforeEvaluate();
            stateInternal.toEvaluate();
        }
    }


    public synchronized Project resolveParameterProject(ParameterContext parameterContext) {
        var parameter = parameterContext.getParameter();
        var annotatedParam = new AnnotatedParam(parameter);

        final long invocationNumber;
        if (invocationNumbers.containsKey(parameter)) {
            invocationNumber = invocationNumbers.get(parameter) + 1;
        } else {
            invocationNumber = 1L;
        }
        invocationNumbers.put(parameter, invocationNumber);

        var parameterProjects = invocationParameterProjects.computeIfAbsent(
            invocationNumber,
            __ -> new LinkedHashMap<>()
        );

        return resolveParameterProject(annotatedParam, parameterProjects);
    }

    @SuppressWarnings("java:S3776")
    private Project resolveParameterProject(
        AnnotatedParam annotatedParam,
        Map<AnnotatedParam, Project> parameterProjects
    ) {
        Project paramProject = parameterProjects.get(annotatedParam);
        if (paramProject != null) {
            return paramProject;
        }

        var dirPrefix = getDirPrefix()
            .newChildPrefix()
            .push(annotatedParam.getName());

        var childProjectOf = annotatedParam.findAnnotation(ChildProjectOf.class);
        if (childProjectOf != null) {
            var parentProjectParamName = childProjectOf.value();
            if (annotatedParam.getName().equals(parentProjectParamName)) {
                throw new IllegalStateException(format(
                    "%s is annotated with @%s that references to itself",
                    annotatedParam,
                    ChildProjectOf.class.getSimpleName()
                ));
            }
            for (var otherParam : annotatedParam.getDeclaringExecutable().getParameters()) {
                if (otherParam.isSynthetic()) {
                    continue;
                }
                var otherAnnotatedParam = new AnnotatedParam(otherParam);
                if (otherParam.getName().equals(parentProjectParamName)) {
                    var parentProject = resolveParameterProject(otherAnnotatedParam, parameterProjects);
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

        var finalProject = paramProject;
        annotatedParam.findRepeatableAnnotations(ApplyPlugin.class).forEach(applyPlugin -> {
            var id = applyPlugin.value();
            if (!id.isEmpty()) {
                finalProject.getPluginManager().apply(id);
            }

            var type = applyPlugin.type();
            if (type != ApplyPlugin.NotSetPluginType.class) {
                finalProject.getPluginManager().apply(type);
            }
        });

        parameterProjects.put(annotatedParam, finalProject);
        return finalProject;
    }

}
