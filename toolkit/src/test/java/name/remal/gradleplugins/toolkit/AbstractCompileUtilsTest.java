package name.remal.gradleplugins.toolkit;

import static java.util.Arrays.asList;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradleplugins.toolkit.testkit.MaxSupportedGradleVersion;
import name.remal.gradleplugins.toolkit.testkit.MinSupportedGradleVersion;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.scala.ScalaCompile;
import org.gradle.jvm.toolchain.JavaCompiler;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class AbstractCompileUtilsTest {

    private final Project project;


    private SourceSet mainSourceSet;
    private JavaCompile compileJava;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply("java");

        val sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        this.mainSourceSet = sourceSets.getByName(MAIN_SOURCE_SET_NAME);
        this.compileJava = project.getTasks().withType(JavaCompile.class)
            .getByName(mainSourceSet.getCompileJavaTaskName());
    }


    @Test
    void getDestinationDir() {
        val destinationDir = AbstractCompileUtils.getDestinationDir(compileJava);
        assertNotNull(destinationDir);
        assertTrue(mainSourceSet.getOutput().getClassesDirs().contains(destinationDir));
    }

    @Test
    void getCompileOptionsOf() {
        List<Class<? extends AbstractCompile>> taskTypes = asList(
            JavaCompile.class,
            GroovyCompile.class,
            ScalaCompile.class
        );

        for (val taskType : taskTypes) {
            val task = project.getTasks().create(taskType.getSimpleName(), taskType);
            val compileOptions = AbstractCompileUtils.getCompileOptionsOf(task);
            assertNotNull(compileOptions, taskType.getName());
        }
    }


    @Test
    @MinSupportedGradleVersion("6.7")
    void getCompilerJavaVersionOrNullOf() {
        val expectedJavaVersion = JavaVersion.values()[JavaVersion.current().ordinal() - 1];

        val metadata = mock(JavaInstallationMetadata.class);
        when(metadata.getLanguageVersion()).thenReturn(JavaLanguageVersion.of(expectedJavaVersion.getMajorVersion()));

        {
            val task = compileJava;

            val javaCompiler = mock(JavaCompiler.class);
            when(javaCompiler.getMetadata()).thenReturn(metadata);
            task.getJavaCompiler().set(javaCompiler);

            val javaVersion = AbstractCompileUtils.getCompilerJavaVersionOrNullOf(task);
            assertEquals(expectedJavaVersion, javaVersion, task.getName());
        }

        {
            project.getPluginManager().apply("groovy");
            val task = project.getTasks().named(
                mainSourceSet.getCompileTaskName("groovy"),
                GroovyCompile.class
            ).get();

            val javaLauncher = mock(JavaLauncher.class);
            when(javaLauncher.getMetadata()).thenReturn(metadata);
            task.getJavaLauncher().set(javaLauncher);

            val javaVersion = AbstractCompileUtils.getCompilerJavaVersionOrNullOf(task);
            assertEquals(expectedJavaVersion, javaVersion, task.getName());
        }
    }

    @Test
    @MaxSupportedGradleVersion("6.6.9999")
    void getCompilerJavaVersionOrNullOf_pre_6_7() {
        List<Class<? extends AbstractCompile>> taskTypes = asList(
            JavaCompile.class,
            GroovyCompile.class,
            ScalaCompile.class
        );

        for (val taskType : taskTypes) {
            val task = project.getTasks().create(taskType.getSimpleName(), taskType);
            val javaVersion = AbstractCompileUtils.getCompilerJavaVersionOrNullOf(task);
            assertEquals(JavaVersion.current(), javaVersion, taskType.getName());
        }
    }

}
