package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.singleton;
import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaModule;
import org.junit.jupiter.api.Test;

class IdeaModuleUtilsTest {

    private final IdeaModule ideaModule;

    public IdeaModuleUtilsTest(Project project) {
        project.getPluginManager().apply("idea");
        this.ideaModule = getExtension(project, IdeaModel.class).getModule();
    }

    @Test
    void testSources() {
        assertThat(IdeaModuleUtils.getTestSourceDirs(ideaModule)).isEmpty();

        val testSourcesDir = normalizeFile(new File("/test-sources"));
        IdeaModuleUtils.setTestSourceDirs(ideaModule, singleton(testSourcesDir));

        assertThat(IdeaModuleUtils.getTestSourceDirs(ideaModule))
            .containsExactly(testSourcesDir);
    }

    @Test
    void testResources() {
        assertThat(IdeaModuleUtils.getTestResourceDirs(ideaModule)).isEmpty();

        val testResourcesDir = normalizeFile(new File("/test-resources"));
        IdeaModuleUtils.setTestResourceDirs(ideaModule, singleton(testResourcesDir));

        assertThat(IdeaModuleUtils.getTestResourceDirs(ideaModule))
            .containsExactly(testResourcesDir);
    }

}
