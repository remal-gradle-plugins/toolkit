package name.remal.gradleplugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.CrossCompileServices.loadCrossCompileService;

import java.io.File;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.gradle.api.tasks.compile.AbstractCompile;

@NoArgsConstructor(access = PRIVATE)
public abstract class AbstractCompileUtils {

    private static final AbstractCompileUtilsMethods METHODS =
        loadCrossCompileService(AbstractCompileUtilsMethods.class);


    @Nullable
    public static File getDestinationDir(AbstractCompile task) {
        return METHODS.getDestinationDir(task);
    }

}
