package name.remal.gradleplugins.toolkit;

import static name.remal.gradleplugins.toolkit.AbstractCompileUtils.getDestinationDir;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.val;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.compile.AbstractCompile;

public interface SourceSetUtils {

    static boolean isProcessedBy(SourceSet sourceSet, SourceTask task) {
        val result = new AtomicBoolean(false);
        val allSource = sourceSet.getAllSource();
        task.getSource().visit(details -> {
            val file = details.getFile();
            if (allSource.contains(file)) {
                result.set(true);
                details.stopVisiting();
            }
        });
        return result.get();
    }

    static boolean isProcessedBy(SourceSet sourceSet, AbstractCopyTask task) {
        val result = new AtomicBoolean(false);
        val allSource = sourceSet.getAllSource();
        task.getSource().getAsFileTree().visit(details -> {
            val file = details.getFile();
            if (allSource.contains(file)) {
                result.set(true);
                details.stopVisiting();
            }
        });
        return result.get();
    }

    static boolean isCompiledBy(SourceSet sourceSet, AbstractCompile task) {
        val destinationDir = getDestinationDir(task);
        if (destinationDir == null) {
            return isProcessedBy(sourceSet, task);
        }

        return sourceSet.getOutput().getClassesDirs().contains(destinationDir);
    }

}
