package name.remal.gradle_plugins.toolkit;

import java.util.Set;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.jetbrains.annotations.Unmodifiable;

interface FileCollectionUtilsMethods {

    @Unmodifiable
    Set<Configuration> getConfigurationsUsedIn(FileCollection fileCollection);

}
