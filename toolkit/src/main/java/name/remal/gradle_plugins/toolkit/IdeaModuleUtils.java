package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyProxy;

import java.io.File;
import java.util.Set;
import lombok.NoArgsConstructor;
import org.gradle.plugins.ide.idea.model.IdeaModule;

@NoArgsConstructor(access = PRIVATE)
public abstract class IdeaModuleUtils {

    private static final IdeaModuleUtilsMethods METHODS = asLazyProxy(
        IdeaModuleUtilsMethods.class,
        () -> loadCrossCompileService(IdeaModuleUtilsMethods.class)
    );


    public static Set<File> getTestSourceDirs(IdeaModule ideaModule) {
        return METHODS.getTestSourceDirs(ideaModule);
    }

    public static void setTestSourceDirs(IdeaModule ideaModule, Set<File> files) {
        METHODS.setTestSourceDirs(ideaModule, files);
    }


    public static Set<File> getTestResourceDirs(IdeaModule ideaModule) {
        return METHODS.getTestResourceDirs(ideaModule);
    }

    public static void setTestResourceDirs(IdeaModule ideaModule, Set<File> files) {
        METHODS.setTestResourceDirs(ideaModule, files);
    }

}
