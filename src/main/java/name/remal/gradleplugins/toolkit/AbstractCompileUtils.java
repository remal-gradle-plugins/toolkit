package name.remal.gradleplugins.toolkit;

import static java.util.Objects.requireNonNull;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.findMethod;

import java.io.File;
import javax.annotation.Nullable;
import name.remal.gradleplugins.toolkit.reflection.TypedMethod0;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.compile.AbstractCompile;

public abstract class AbstractCompileUtils {

    @Nullable
    private static final TypedMethod0<AbstractCompile, DirectoryProperty> abstractCompileGetDestinationDirectory =
        findMethod(AbstractCompile.class, DirectoryProperty.class, "getDestinationDirectory");

    @Nullable
    private static final TypedMethod0<AbstractCompile, File> abstractCompileGetDestinationDir =
        findMethod(AbstractCompile.class, File.class, "getDestinationDir");

    @Nullable
    public static File getDestinationDir(AbstractCompile task) {
        if (abstractCompileGetDestinationDirectory != null) {
            return requireNonNull(abstractCompileGetDestinationDirectory.invoke(task))
                .getAsFile()
                .getOrNull();

        } else if (abstractCompileGetDestinationDir != null) {
            return abstractCompileGetDestinationDir.invoke(task);

        } else {
            throw new IllegalStateException(
                "Both 'getDestinationDirectory' and 'getDestinationDir' methods can't be found for task: " + task
            );
        }
    }


    private AbstractCompileUtils() {
    }

}
