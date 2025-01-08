package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyProxy;

import java.io.File;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.gradle.api.tasks.compile.AbstractCompile;

@NoArgsConstructor(access = PRIVATE)
public abstract class AbstractCompileUtils {

    private static final AbstractCompileUtilsMethods METHODS = asLazyProxy(
        AbstractCompileUtilsMethods.class,
        () -> loadCrossCompileService(AbstractCompileUtilsMethods.class)
    );


    @Nullable
    public static File getDestinationDir(AbstractCompile task) {
        return METHODS.getDestinationDir(task);
    }

}
