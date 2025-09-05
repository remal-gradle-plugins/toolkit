package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.getKotlinCompileDestinationDirectory;
import static name.remal.gradle_plugins.toolkit.KotlinPluginUtils.getKotlinCompileSources;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@TagKotlinPlugin
class KotlinPluginUtilsTest extends SourceSetUtilsTestBase {

    KotlinPluginUtilsTest(Project project) {
        super(project);

        project.getPluginManager().apply("org.jetbrains.kotlin.jvm");
    }

    @Nested
    class GetKotlinCompileSources {

        @Test
        void javaCompile() {
            var task = project.getTasks().withType(JavaCompile.class)
                .getByName(mainSourceSet.getCompileJavaTaskName());
            var sources = getKotlinCompileSources(task);
            assertNull(sources);
        }

        @Test
        void kotlinCompile() {
            var task = project.getTasks()
                .getByName(mainSourceSet.getCompileTaskName("kotlin"));
            var sources = getKotlinCompileSources(task);
            assertNotNull(sources);
            assertThat(mainSourceSet.getAllSource())
                .containsAll(sources);
        }

    }

    @Nested
    class GetKotlinCompileDestinationDirectory {

        @Test
        void javaCompile() {
            var task = project.getTasks().withType(JavaCompile.class)
                .getByName(mainSourceSet.getCompileJavaTaskName());
            var destinationDirectory = getKotlinCompileDestinationDirectory(task);
            assertNull(destinationDirectory);
        }

        @Test
        void kotlinCompile() {
            var task = project.getTasks()
                .getByName(mainSourceSet.getCompileTaskName("kotlin"));
            var destinationDirectory = getKotlinCompileDestinationDirectory(task);
            assertNotNull(destinationDirectory);
            assertThat(mainSourceSet.getOutput().getClassesDirs())
                .contains(destinationDirectory.getAsFile().get());
        }

    }

}
