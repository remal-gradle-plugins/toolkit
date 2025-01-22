package name.remal.gradle_plugins.toolkit;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.junit.jupiter.api.Test;

class GradleManagedObjectsUtilsTest {

    @SuppressWarnings("UnusedMethod")
    protected interface ManagedObject {

        Property<String> getStringProperty();

        RegularFileProperty getRegularFileProperty();

        DirectoryProperty getDirectoryProperty();

        ListProperty<String> getListProperty();

        SetProperty<String> getSetProperty();

        MapProperty<String, String> getMapProperty();

        ConfigurableFileCollection getFileCollection();

        ConfigurableFileTree getFileTree();

        @org.gradle.api.tasks.Nested
        NestedManagedObject getNested();

    }

    @SuppressWarnings("UnusedMethod")
    public interface NestedManagedObject {

        Property<Integer> getNestedProperty();

    }


    @Test
    @SuppressWarnings("JUnitMalformedDeclaration")
    void copyManagedProperties(Project project) throws Throwable {
        var source = project.getObjects().newInstance(ManagedObject.class);
        source.getStringProperty().set("string");
        source.getRegularFileProperty().set(project.file("build.gradle"));
        source.getDirectoryProperty().set(project.getLayout().getProjectDirectory());
        source.getListProperty().set(List.of("a", "a"));
        source.getSetProperty().set(List.of("a", "a"));
        source.getMapProperty().set(singletonMap("key", "value"));
        source.getFileCollection().from(project.file("build.gradle"));
        source.getFileTree().setDir(project.getLayout().getProjectDirectory());
        source.getNested().getNestedProperty().set(13);

        createDirectories(source.getFileTree().getDir().toPath());
        createFile(source.getFileTree().getDir().toPath().resolve("file"));
        assertThat(source.getFileTree().getFiles()).isNotEmpty();

        var target = project.getObjects().newInstance(ManagedObject.class);
        GradleManagedObjectsUtils.copyManagedProperties(source, target);
        assertEquals(source.getStringProperty().get(), target.getStringProperty().get());
        assertEquals(source.getRegularFileProperty().get(), target.getRegularFileProperty().get());
        assertEquals(source.getDirectoryProperty().get(), target.getDirectoryProperty().get());
        assertEquals(source.getListProperty().get(), target.getListProperty().get());
        assertEquals(source.getSetProperty().get(), target.getSetProperty().get());
        assertEquals(source.getMapProperty().get(), target.getMapProperty().get());
        assertEquals(source.getFileCollection().getFiles(), target.getFileCollection().getFiles());
        // ConfigurableFileTree can't be copied
        var targetFileTree = target.getFileTree();
        assertThrows(InvalidUserDataException.class, targetFileTree::getFiles);
        assertEquals(source.getNested().getNestedProperty().get(), target.getNested().getNestedProperty().get());
    }

    @Test
    void isGradleManagedType() {
        class LocalClass {
        }

        ImmutableMap.<Class<?>, Boolean>builder()
            .put(int.class, false)
            .put(Integer.class, false)
            .put(ManagedObject[].class, false)
            .put(TaskAction.class, false)
            .put(PathSensitivity.class, false)
            .put(Object.class, false)
            .put(LocalClass.class, false)
            .put(InnerClass.class, false)
            .put(EmptyInnerStaticClass.class, false)
            .put(EmptyInnerAbstractStaticClass.class, true)
            .put(EmptyInnerAbstractStaticClassWithField.class, false)
            .put(EmptyInnerInterface.class, true)
            .put(NotManagedObject.class, false)
            .put(ManagedObject.class, true)
            .put(NestedManagedObject.class, true)
            .build()
            .forEach((clazz, expectedResult) ->
                assertEquals(
                    expectedResult,
                    GradleManagedObjectsUtils.isGradleManagedType(clazz),
                    clazz.toString()
                )
            );
    }


    @SuppressWarnings("UnusedMethod")
    protected interface NotManagedObject {

        List<String> getList();

    }


    @SuppressWarnings({"InnerClassMayBeStatic", "ClassCanBeStatic"})
    protected class InnerClass {
    }

    protected static class EmptyInnerStaticClass {
    }

    protected abstract static class EmptyInnerAbstractStaticClass {
    }

    protected abstract static class EmptyInnerAbstractStaticClassWithField {

        public String value = "123";

    }

    protected interface EmptyInnerInterface { }

}
