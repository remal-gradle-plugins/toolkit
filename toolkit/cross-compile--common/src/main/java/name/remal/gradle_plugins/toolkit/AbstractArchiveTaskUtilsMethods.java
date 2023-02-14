package name.remal.gradle_plugins.toolkit;

import java.io.File;
import javax.annotation.Nullable;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

interface AbstractArchiveTaskUtilsMethods {

    @Nullable
    File getArchiveFile(AbstractArchiveTask task);

}
