package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import java.io.File;
import javax.annotation.Nullable;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

@AutoService(AbstractArchiveTaskUtilsMethods.class)
final class AbstractArchiveTaskUtilsMethods_5_0 implements AbstractArchiveTaskUtilsMethods {

    @Override
    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public File getArchiveFile(AbstractArchiveTask task) {
        return task.getArchivePath();
    }

}
