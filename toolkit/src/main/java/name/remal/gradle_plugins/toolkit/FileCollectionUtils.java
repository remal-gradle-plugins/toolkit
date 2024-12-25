package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.file.FileCollection;
import org.jetbrains.annotations.Unmodifiable;

@NoArgsConstructor(access = PRIVATE)
public abstract class FileCollectionUtils {

    private static final FileCollectionUtilsMethods METHODS = loadCrossCompileService(FileCollectionUtilsMethods.class);

    @Unmodifiable
    public static Set<Configuration> getConfigurationsUsedIn(@Nullable FileCollection fileCollection) {
        if (fileCollection == null) {
            return emptySet();
        }

        if (fileCollection instanceof Configuration) {
            return singleton((Configuration) fileCollection);
        }

        return METHODS.getConfigurationsUsedIn(fileCollection);
    }

    @Unmodifiable
    public static Map<File, ModuleVersionIdentifier> getModuleVersionIdentifiersForFilesIn(
        @Nullable FileCollection fileCollection
    ) {
        if (fileCollection == null) {
            return emptyMap();
        }

        val result = new LinkedHashMap<File, ModuleVersionIdentifier>();

        val files = fileCollection.getFiles();
        getConfigurationsUsedIn(fileCollection).stream()
            .filter(Configuration::isCanBeResolved)
            .map(Configuration::getResolvedConfiguration)
            .map(ResolvedConfiguration::getResolvedArtifacts)
            .flatMap(Collection::stream)
            .forEach(artifact -> {
                val artifactFile = artifact.getFile();
                if (files.contains(artifactFile)) {
                    result.putIfAbsent(artifactFile, artifact.getModuleVersion().getId());
                }
            });

        return ImmutableMap.copyOf(result);
    }

}
