package name.remal.gradleplugins.toolkit;

import java.io.File;
import java.util.Set;
import org.gradle.plugins.ide.idea.model.IdeaModule;

interface IdeaModuleUtilsMethods {

    Set<File> getTestSourceDirs(IdeaModule ideaModule);

    void setTestSourceDirs(IdeaModule ideaModule, Set<File> files);


    Set<File> getTestResourceDirs(IdeaModule ideaModule);

    void setTestResourceDirs(IdeaModule ideaModule, Set<File> files);

}
