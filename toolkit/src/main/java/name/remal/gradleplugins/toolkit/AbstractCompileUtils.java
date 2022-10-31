package name.remal.gradleplugins.toolkit;

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.reflection.MembersFinder.findMethod;

import java.io.File;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradleplugins.toolkit.reflection.TypedMethod0;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;

@NoArgsConstructor(access = PRIVATE)
public abstract class AbstractCompileUtils {

    @Nullable
    private static final TypedMethod0<AbstractCompile, DirectoryProperty> getDestinationDirectoryMethod =
        findMethod(AbstractCompile.class, DirectoryProperty.class, "getDestinationDirectory");

    @Nullable
    private static final TypedMethod0<AbstractCompile, File> compileGetDestinationDirMethod =
        findMethod(AbstractCompile.class, File.class, "getDestinationDir");

    @Nullable
    public static File getDestinationDir(AbstractCompile task) {
        if (getDestinationDirectoryMethod != null) {
            return requireNonNull(getDestinationDirectoryMethod.invoke(task))
                .getAsFile()
                .getOrNull();

        } else if (compileGetDestinationDirMethod != null) {
            return compileGetDestinationDirMethod.invoke(task);

        } else {
            throw new IllegalStateException(
                "Both 'getDestinationDirectory' and 'getDestinationDir' methods can't be found for task: " + task
            );
        }
    }

    @Nullable
    public static CompileOptions getCompileOptionsOf(AbstractCompile task) {
        @SuppressWarnings("unchecked")
        val getter = findMethod((Class<AbstractCompile>) task.getClass(), CompileOptions.class, "getOptions");
        if (getter != null) {
            return getter.invoke(task);
        }

        return null;
    }

}
