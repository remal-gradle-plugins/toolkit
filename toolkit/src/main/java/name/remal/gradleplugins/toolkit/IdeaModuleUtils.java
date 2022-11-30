package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.findMethod;

import java.io.File;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import name.remal.gradleplugins.toolkit.reflection.TypedMethod0;
import name.remal.gradleplugins.toolkit.reflection.TypedVoidMethod1;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.plugins.ide.idea.model.IdeaModule;

@NoArgsConstructor(access = PRIVATE)
public abstract class IdeaModuleUtils {

    @Nullable
    private static final TypedMethod0<IdeaModule, ConfigurableFileCollection> getTestSourcesMethod =
        findMethod(IdeaModule.class, ConfigurableFileCollection.class, "getTestSources");

    @Nullable
    @SuppressWarnings("rawtypes")
    private static final TypedMethod0<IdeaModule, Set> getTestSourceDirsMethod =
        findMethod(IdeaModule.class, Set.class, "getTestSourceDirs");

    @SuppressWarnings("unchecked")
    public static Set<File> getTestSourceDirs(IdeaModule ideaModule) {
        if (getTestSourcesMethod != null) {
            return getTestSourcesMethod.invoke(ideaModule).getFiles();

        } else if (getTestSourceDirsMethod != null) {
            return getTestSourceDirsMethod.invoke(ideaModule);

        } else {
            throw new IllegalStateException(
                "Both 'getTestSources' and 'getTestSourceDirs' methods can't be found: " + ideaModule
            );
        }
    }

    @Nullable
    @SuppressWarnings("rawtypes")
    private static final TypedVoidMethod1<IdeaModule, Set> setTestSourceDirsMethod =
        findMethod(IdeaModule.class, "setTestSourceDirs", Set.class);

    public static void setTestSourceDirs(IdeaModule ideaModule, Set<File> files) {
        if (getTestSourcesMethod != null) {
            getTestSourcesMethod.invoke(ideaModule).setFrom(files);

        } else if (setTestSourceDirsMethod != null) {
            setTestSourceDirsMethod.invoke(ideaModule, files);

        } else {
            throw new IllegalStateException(
                "Both 'getTestSources' and 'setTestSourceDirs' methods can't be found: " + ideaModule
            );
        }
    }


    @Nullable
    private static final TypedMethod0<IdeaModule, ConfigurableFileCollection> getTestResourcesMethod =
        findMethod(IdeaModule.class, ConfigurableFileCollection.class, "getTestResources");

    @Nullable
    @SuppressWarnings("rawtypes")
    private static final TypedMethod0<IdeaModule, Set> getTestResourceDirsMethod =
        findMethod(IdeaModule.class, Set.class, "getTestResourceDirs");

    @SuppressWarnings("unchecked")
    public static Set<File> getTestResourceDirs(IdeaModule ideaModule) {
        if (getTestResourcesMethod != null) {
            return getTestResourcesMethod.invoke(ideaModule).getFiles();

        } else if (getTestResourceDirsMethod != null) {
            return getTestResourceDirsMethod.invoke(ideaModule);

        } else {
            throw new IllegalStateException(
                "Both 'getTestResources' and 'getTestResourceDirs' methods can't be found: " + ideaModule
            );
        }
    }

    @Nullable
    @SuppressWarnings("rawtypes")
    private static final TypedVoidMethod1<IdeaModule, Set> setTestResourceDirsMethod =
        findMethod(IdeaModule.class, "setTestResourceDirs", Set.class);

    public static void setTestResourceDirs(IdeaModule ideaModule, Set<File> files) {
        if (getTestResourcesMethod != null) {
            getTestResourcesMethod.invoke(ideaModule).setFrom(files);

        } else if (setTestResourceDirsMethod != null) {
            setTestResourceDirsMethod.invoke(ideaModule, files);

        } else {
            throw new IllegalStateException(
                "Both 'getTestResources' and 'setTestResourceDirs' methods can't be found: " + ideaModule
            );
        }
    }

}
