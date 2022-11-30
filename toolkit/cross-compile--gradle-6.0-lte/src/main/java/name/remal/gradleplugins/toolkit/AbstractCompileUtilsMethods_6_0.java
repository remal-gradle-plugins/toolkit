package name.remal.gradleplugins.toolkit;

import com.google.auto.service.AutoService;
import java.io.File;
import javax.annotation.Nullable;
import org.gradle.api.tasks.compile.AbstractCompile;

@AutoService(AbstractCompileUtilsMethods.class)
final class AbstractCompileUtilsMethods_6_0 implements AbstractCompileUtilsMethods {

    @Nullable
    @Override
    public File getDestinationDir(AbstractCompile task) {
        return task.getDestinationDir();
    }

}
