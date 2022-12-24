package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import java.io.File;
import java.util.Set;
import org.gradle.plugins.ide.idea.model.IdeaModule;

@AutoService(IdeaModuleUtilsMethods.class)
final class IdeaModuleUtilsMethodsDefault implements IdeaModuleUtilsMethods {

    @Override
    public Set<File> getTestSourceDirs(IdeaModule ideaModule) {
        return ideaModule.getTestSources().getFiles();
    }

    @Override
    public void setTestSourceDirs(IdeaModule ideaModule, Set<File> files) {
        ideaModule.getTestSources().setFrom(files);
    }


    @Override
    public Set<File> getTestResourceDirs(IdeaModule ideaModule) {
        return ideaModule.getTestResources().getFiles();
    }

    @Override
    public void setTestResourceDirs(IdeaModule ideaModule, Set<File> files) {
        ideaModule.getTestResources().setFrom(files);
    }

}
