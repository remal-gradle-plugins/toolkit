package name.remal.gradle_plugins.toolkit;

import java.io.File;
import javax.annotation.Nullable;
import org.gradle.api.tasks.compile.AbstractCompile;

interface AbstractCompileUtilsMethods {

    @Nullable
    File getDestinationDir(AbstractCompile task);

}
