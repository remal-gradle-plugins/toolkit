package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import java.io.File;
import javax.annotation.Nullable;
import lombok.val;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

@AutoService(AbstractArchiveTaskUtilsMethods.class)
final class AbstractArchiveTaskUtilsMethodsDefault implements AbstractArchiveTaskUtilsMethods {

    @Override
    @Nullable
    public File getArchiveFile(AbstractArchiveTask task) {
        val archiveFile = task.getArchiveFile().getOrNull();
        return archiveFile != null ? archiveFile.getAsFile() : null;
    }

}
