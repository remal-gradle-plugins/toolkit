package name.remal.gradleplugins.toolkit;

import java.io.File;
import javax.annotation.Nullable;
import org.gradle.api.tasks.compile.AbstractCompile;

interface AbstractCompileUtilsMethods {

    @Nullable
    File getDestinationDir(AbstractCompile task);

}
