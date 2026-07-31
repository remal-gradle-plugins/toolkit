package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.GradleUtils.beforeProjectWithLifecycleSupport;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;

import com.google.errorprone.annotations.ForOverride;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

/**
 * Base class for a plugin that can be applied to either a {@link Project} or the root {@link Settings}.
 *
 * <p>When applied to {@link Settings}, the plugin registers itself to be applied to every project of
 * the build instead of configuring the {@link Settings} object directly, using
 * {@link GradleUtils#beforeProjectWithLifecycleSupport}. This is safe to use with Gradle's Isolated
 * Projects feature, unlike the common {@code allprojects { apply plugin: ... } } pattern, which reaches
 * into other projects' mutable state.
 */
public abstract class AbstractSettingsAwarePlugin implements Plugin<Object> {

    @Override
    public final void apply(Object target) {
        if (target instanceof Settings) {
            applyToSettings((Settings) target);
        } else if (target instanceof Project) {
            applyToProject((Project) target);
        } else {
            @SuppressWarnings("ConstantValue")
            var targetClassName = target != null ? target.getClass().getName() : "null";
            throw new GradleException(
                "This plugin can only be applied to a Project or Settings, got: " + targetClassName
            );
        }
    }

    /**
     * Returns the plugin class to apply to each project when this plugin is applied to {@link Settings}.
     *
     * <p>Defaults to {@link #getClass()} with any Gradle-generated decoration subclass unwrapped, since
     * a plugin instantiated with injected services (e.g. via {@link Inject}-annotated abstract getters)
     * is actually an instance of a dynamically generated subclass, not the class it was declared as.
     */
    @ForOverride
    protected Class<?> getProjectPluginClass() {
        return unwrapGeneratedSubclass(getClass());
    }

    /**
     * Propagates this plugin to every project of the build.
     *
     * <p>The default implementation doesn't configure {@link Settings} itself. It only registers
     * {@link #getProjectPluginClass()} to be applied to each project via
     * {@link GradleUtils#beforeProjectWithLifecycleSupport}. Override this if the plugin also needs to
     * configure the {@link Settings} object itself.
     */
    protected void applyToSettings(Settings settings) {
        var pluginClass = getProjectPluginClass();
        beforeProjectWithLifecycleSupport(
            settings.getGradle(),
            project -> project.getPluginManager().apply(pluginClass)
        );
    }

    /**
     * Configures the given project. Called once per project, regardless of whether the plugin was
     * applied directly to that project or propagated from a {@link Settings} application.
     */
    protected abstract void applyToProject(Project project);

}
