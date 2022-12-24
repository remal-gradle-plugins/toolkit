package name.remal.gradle_plugins.toolkit;

import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.junit.jupiter.api.Test;

class AbstractCompileUtilsTest {

    private final SourceSet mainSourceSet;
    private final JavaCompile compileJava;

    public AbstractCompileUtilsTest(Project project) {
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

}
