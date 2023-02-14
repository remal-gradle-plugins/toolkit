package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;

import java.io.File;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

@NoArgsConstructor(access = PRIVATE)
public abstract class AbstractArchiveTaskUtils {

    private static final AbstractArchiveTaskUtilsMethods METHODS =
        loadCrossCompileService(AbstractArchiveTaskUtilsMethods.class);


    @Nullable
    public static File getArchivePath(AbstractArchiveTask task) {
        return METHODS.getArchiveFile(task);
    }

}
