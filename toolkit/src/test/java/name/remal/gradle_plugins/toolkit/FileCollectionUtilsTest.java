package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newOutputStream;
import static java.util.jar.Attributes.Name.MANIFEST_VERSION;
import static java.util.jar.JarFile.MANIFEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.testkit.MinTestableGradleVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class FileCollectionUtilsTest {

    final Project project;

    @Test
    void getConfigurationsUsedIn() {
        FileCollection fileCollection = project.files();

        var conf1 = createConfiguration("conf1");
        fileCollection = fileCollection.plus(conf1);

        fileCollection.filter(__ -> true);

        var conf2 = createConfiguration("conf2");
        fileCollection = fileCollection.plus(conf2);

        fileCollection = fileCollection.plus(project.files().getAsFileTree());

        assertThat(FileCollectionUtils.getConfigurationsUsedIn(fileCollection))
            .containsExactlyInAnyOrder(conf1, conf2);
    }

    @Test
    @MinTestableGradleVersion("6.4")
    void getConfigurationsUsedIn_with_minus() {
        FileCollection fileCollection = project.files();

        var conf1 = createConfiguration("conf1");
        fileCollection = fileCollection.plus(conf1);

        var conf2 = createConfiguration("conf2");
        fileCollection = fileCollection.plus(conf2);

        fileCollection = fileCollection.plus(project.files().getAsFileTree());

        fileCollection = fileCollection.minus(conf1);

        fileCollection = fileCollection.plus(project.files(project.provider(project::getBuildFile)));

        assertThat(FileCollectionUtils.getConfigurationsUsedIn(fileCollection))
            .contains(conf2 /* conf1 can be here in different Gradle versions */);
    }


    @SneakyThrows
    private Configuration createConfiguration(String name) {
        /*
         * Empty configurations don't call any methods of `FileCollectionStructureVisitor` in Gradle <=6.3.
         * This code creates a configuration with the simplest resolvable dependency.
         */

        var dependencyFile = new File(project.getRootDir(), name + ".jar");
        createDirectories(dependencyFile.getParentFile().toPath());
        try (var out = new ZipOutputStream(newOutputStream(dependencyFile.toPath()))) {
            var manifest = new Manifest();
            manifest.getMainAttributes().putValue(MANIFEST_VERSION.toString(), "1.0");
            out.putNextEntry(new ZipEntry(MANIFEST_NAME));
            manifest.write(out);
        }

        var configuration = project.getConfigurations().create(name, conf -> {
            conf.setCanBeResolved(true);

            conf.getDependencies().add(
                project.getDependencies().create(
                    project.files(dependencyFile)
                )
            );
        });

        return configuration;
    }

}
