package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.Collection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;

@AutoService(SourceSetSourceDirectorySetsGetter.class)
class SourceSetSourceDirectorySetsGetterExtensions implements SourceSetSourceDirectorySetsGetter {

    @Override
    public Collection<SourceDirectorySet> get(SourceSet sourceSet) {
        var result = new ArrayList<SourceDirectorySet>();

        var extensions = sourceSet.getExtensions();
        for (var extensionSchema : extensions.getExtensionsSchema().getElements()) {
            var publicType = extensionSchema.getPublicType().getConcreteClass();
            if (SourceDirectorySet.class.isAssignableFrom(publicType)) {
                var extensionName = extensionSchema.getName();
                var extension = (SourceDirectorySet) extensions.getByName(extensionName);
                result.add(extension);
            }
        }

        return result;
    }

}
