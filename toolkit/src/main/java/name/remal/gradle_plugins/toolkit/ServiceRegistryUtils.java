package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.SettingsInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.internal.service.ServiceRegistry;
import org.jspecify.annotations.Nullable;

@ReliesOnInternalGradleApi
@NoArgsConstructor(access = PRIVATE)
public abstract class ServiceRegistryUtils {

    public static ServiceRegistry getServiceRegistryFor(Gradle gradle) {
        return ((GradleInternal) gradle).getServices();
    }

    public static ServiceRegistry getServiceRegistryFor(Settings settings) {
        return ((SettingsInternal) settings).getServices();
    }

    public static ServiceRegistry getServiceRegistryFor(Project project) {
        return ((ProjectInternal) project).getServices();
    }


    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T findService(Gradle gradle, Class<T> type) {
        return (T) getServiceRegistryFor(gradle).find(type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T findService(Settings settings, Class<T> type) {
        return (T) getServiceRegistryFor(settings).find(type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T findService(Project project, Class<T> type) {
        return (T) getServiceRegistryFor(project).find(type);
    }


    public static <T> T getService(Gradle gradle, Class<T> type) {
        return getServiceRegistryFor(gradle).get(type);
    }

    public static <T> T getService(Settings settings, Class<T> type) {
        return getServiceRegistryFor(settings).get(type);
    }

    public static <T> T getService(Project project, Class<T> type) {
        return getServiceRegistryFor(project).get(type);
    }

}
