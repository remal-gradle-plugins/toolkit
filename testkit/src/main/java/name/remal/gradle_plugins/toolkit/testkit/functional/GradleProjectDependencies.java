package name.remal.gradle_plugins.toolkit.testkit.functional;

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.generate_sources.generators.java_like.JavaLikeContent;
import name.remal.gradle_plugins.toolkit.testkit.TestClasspath;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleProjectDependencies {

    public static void addLibrariesAsConfigurationDependency(
        JavaLikeContent<?> buildFileBlock,
        String configurationName,
        Collection<String> libraryNotations
    ) {
        if (libraryNotations.isEmpty()) {
            return;
        }

        buildFileBlock.line(
            "dependencies { %s(files(%s)) }",
            configurationName,
            libraryNotations.stream()
                .map(TestClasspath::getTestClasspathLibraryFilePaths)
                .flatMap(Collection::stream)
                .map(Path::toString)
                .distinct()
                .map(path -> '"' + buildFileBlock.escapeString(path) + '"')
                .collect(joining(", "))
        );
    }

    public static void addLibrariesAsConfigurationDependency(
        JavaLikeContent<?> buildFileBlock,
        String configurationName,
        String... libraryNotations
    ) {
        addLibrariesAsConfigurationDependency(
            buildFileBlock,
            configurationName,
            List.of(libraryNotations)
        );
    }

}
