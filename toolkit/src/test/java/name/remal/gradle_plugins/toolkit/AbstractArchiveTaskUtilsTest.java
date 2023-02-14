package name.remal.gradle_plugins.toolkit;

import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.junit.jupiter.api.Test;

class AbstractArchiveTaskUtilsTest {

    private final Jar jar;

    public AbstractArchiveTaskUtilsTest(Project project) {
        project.getPluginManager().apply("java");

        val sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        val mainSourceSet = sourceSets.getByName(MAIN_SOURCE_SET_NAME);
        this.jar = project.getTasks().withType(Jar.class)
            .getByName(mainSourceSet.getJarTaskName());
    }


    @Test
    void getArchivePath() {
        val archivePath = AbstractArchiveTaskUtils.getArchivePath(jar);
        assertNotNull(archivePath);
    }

}
