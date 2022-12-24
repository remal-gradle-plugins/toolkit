package name.remal.gradle_plugins.toolkit;

import com.google.auto.service.AutoService;
import java.io.File;
import javax.annotation.Nullable;
import org.gradle.api.tasks.compile.AbstractCompile;

@AutoService(AbstractCompileUtilsMethods.class)
final class AbstractCompileUtilsMethodsDefault implements AbstractCompileUtilsMethods {

    @Nullable
    @Override
    public File getDestinationDir(AbstractCompile task) {
        return task.getDestinationDirectory().getAsFile().getOrNull();
    }

}
