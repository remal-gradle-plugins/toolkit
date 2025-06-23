package name.remal.gradle_plugins.toolkit;

import java.util.Collection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;

interface SourceSetSourceDirectorySetsGetter {

    Collection<SourceDirectorySet> get(SourceSet sourceSet);

}
