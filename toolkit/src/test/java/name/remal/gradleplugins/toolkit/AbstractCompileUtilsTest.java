package name.remal.gradleplugins.toolkit;

import static java.util.Arrays.asList;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.scala.ScalaCompile;
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

}
