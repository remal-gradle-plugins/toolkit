package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.createFile;
import static name.remal.gradle_plugins.toolkit.PathUtils.createParentDirectories;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectories;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.work.NormalizeLineEndings;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class TaskPropertiesUtilsTest {

    final Project project;

    @Test
    void test() throws Throwable {
        val task = project.getTasks().register("test").get();
        val additionalProps = project.getObjects().newInstance(AdditionalProperties.class);
        TaskPropertiesUtils.registerTaskProperties(task, additionalProps);

        val nestedProviderProperties = project.getObjects().newInstance(NestedProperties.class);
        nestedProviderProperties.getIntegers().addAll(4, 5, 6);

        additionalProps.getStrings().addAll("a", "b", "c");
        additionalProps.getInputDirectory().set(new File("getInputDirectory"));
        additionalProps.getInputFile().set(new File("getInputFile"));
        additionalProps.getInputFiles().from(new File("getInputFiles"));
        additionalProps.getNestedProperties().getIntegers().addAll(1, 2, 3);
        additionalProps.getNestedProviderProperties().set(nestedProviderProperties);
        additionalProps.getOutputDirectories().from(new File("getOutputDirectories"));
        additionalProps.getOutputDirectory().set(new File("getOutputDirectory"));
        additionalProps.getOutputFile().set(new File("getOutputFile"));
        additionalProps.getOutputFiles().from(new File("getOutputFiles"));

        createFile(createParentDirectories(project.file("getInputDirectory/file").toPath()));
        createFile(createParentDirectories(project.file("getOutputDirectories/file").toPath()));
        createFile(createParentDirectories(project.file("getOutputDirectory/file").toPath()));

        assertThat(task.getInputs().getProperties())
            .containsEntry("strings", additionalProps.getStrings())
            .containsEntry("nestedProperties.integers", additionalProps.getNestedProperties().getIntegers())
            .containsEntry("nestedProviderProperties.integers", nestedProviderProperties.getIntegers())
            .doesNotContainKey("internal")
            .doesNotContainKey("console")
        ;

        assertThat(task.getInputs().getFiles().getFiles()).contains(
            project.file("getInputDirectory/file"),
            project.file("getInputFiles"),
            project.file("getInputFile")
        );

        assertThat(task.getOutputs().getFiles().getFiles()).contains(
            project.file("getOutputDirectories"),
            project.file("getOutputDirectory"),
            project.file("getOutputFile"),
            project.file("getOutputFiles")
        );
    }


    protected interface AdditionalProperties {

        @Input
        @org.gradle.api.tasks.Optional
        ListProperty<String> getStrings();

        @InputDirectory
        @IgnoreEmptyDirectories
        DirectoryProperty getInputDirectory();

        @InputFile
        @NormalizeLineEndings
        RegularFileProperty getInputFile();

        @InputFiles
        ConfigurableFileCollection getInputFiles();

        @org.gradle.api.tasks.Nested
        NestedProperties getNestedProperties();

        @org.gradle.api.tasks.Nested
        Property<NestedProperties> getNestedProviderProperties();

        @OutputDirectories
        ConfigurableFileCollection getOutputDirectories();

        @OutputDirectory
        DirectoryProperty getOutputDirectory();

        @OutputFile
        RegularFileProperty getOutputFile();

        @OutputFiles
        ConfigurableFileCollection getOutputFiles();

        @Internal
        Property<Long> getInternal();

        @Console
        Property<Boolean> getConsole();

    }

    protected interface NestedProperties {

        @Input
        ListProperty<Integer> getIntegers();

    }

}
