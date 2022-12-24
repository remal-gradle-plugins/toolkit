package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import org.gradle.plugins.ide.idea.model.IdeaModule;

@AutoService(IdeaModuleUtilsMethods.class)
@SuppressWarnings("java:S1121")
final class IdeaModuleUtilsMethods_7_3 implements IdeaModuleUtilsMethods {

    @Override
    public Set<File> getTestSourceDirs(IdeaModule ideaModule) {
        Set<File> result = ideaModule.getTestSourceDirs();
        if (result == null) {
            ideaModule.setTestSourceDirs(result = new LinkedHashSet<>());
        }
        return result;
    }

    @Override
    public void setTestSourceDirs(IdeaModule ideaModule, Set<File> files) {
        ideaModule.setTestSourceDirs(files);
    }


    @Override
    public Set<File> getTestResourceDirs(IdeaModule ideaModule) {
        Set<File> result = ideaModule.getTestResourceDirs();
        if (result == null) {
            ideaModule.setTestResourceDirs(result = new LinkedHashSet<>());
        }
        return result;
    }

    @Override
    public void setTestResourceDirs(IdeaModule ideaModule, Set<File> files) {
        ideaModule.setTestResourceDirs(files);
    }

}
