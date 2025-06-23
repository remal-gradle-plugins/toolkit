package name.remal.gradle_plugins.toolkit;

import static java.lang.reflect.Modifier.isStatic;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.Collection;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;

@AutoService(SourceSetSourceDirectorySetsGetter.class)
class SourceSetSourceDirectorySetsGetterConventions implements SourceSetSourceDirectorySetsGetter {

    @Override
    @ReliesOnInternalGradleApi
    @SneakyThrows
    @SuppressWarnings({"deprecation", "java:S3776"})
    public Collection<SourceDirectorySet> get(SourceSet sourceSet) {
        var result = new ArrayList<SourceDirectorySet>();

        if (sourceSet instanceof org.gradle.api.internal.HasConvention) {
            var convention = ((org.gradle.api.internal.HasConvention) sourceSet).getConvention();
            for (var pluginEntry : convention.getPlugins().entrySet()) {
                var plugin = pluginEntry.getValue();
                for (var pluginMethod : plugin.getClass().getMethods()) {
                    if (isStatic(pluginMethod.getModifiers())
                        || pluginMethod.getParameterCount() != 0
                        || !SourceDirectorySet.class.isAssignableFrom(pluginMethod.getReturnType())
                    ) {
                        continue;
                    }

                    var sourceDirectorySet = (SourceDirectorySet) pluginMethod.invoke(plugin);
                    result.add(sourceDirectorySet);
                }
            }
        }

        return result;
    }
}
