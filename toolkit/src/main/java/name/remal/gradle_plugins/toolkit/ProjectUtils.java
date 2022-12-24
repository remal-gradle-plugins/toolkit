package name.remal.gradle_plugins.toolkit;

import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;

@NoArgsConstructor(access = PRIVATE)
public abstract class ProjectUtils {

    public static Path getTopLevelDirOf(Project project) {
        val rootProject = project.getRootProject();
        val projectDir = normalizePath(rootProject.getProjectDir().toPath());
        if (isBuildSrcProject(project)) {
            return projectDir.getParent();
        } else {
            return projectDir;
        }
    }

    public static boolean isBuildSrcProject(Project project) {
        project = project.getRootProject();
        return project.getName().equals("buildSrc");
    }

    public static void afterEvaluateOrNow(Project project, Action<? super Project> action) {
        if (project.getState().getExecuted()) {
            action.execute(project);
        } else {
            project.afterEvaluate(action);
        }
    }


    /**
     * <p>Gradle configuration cache doesn't allow using {@link Project} at execution time.</p>
     * <p>Usage:</p>
     * <pre>{@code
     * classpathFileTree(
     *     files -> getObjectFactory().fileCollection().from(files),
     *     getArchiveOperations()::zipTree,
     *     classpathFiles
     * )
     * }</pre>
     */
    public static FileTree newClasspathFileTree(
        Function<Iterable<File>, FileCollection> fileCollectionFactory,
        Function<File, FileTree> zipTreeFactory,
        Iterable<File> files
    ) {
        AtomicReference<FileTree> resultRef = new AtomicReference<>();
        Consumer<FileTree> addToResult = fileTree -> {
            FileTree result = resultRef.get();
            if (result == null) {
                result = fileTree;
            } else {
                result = result.plus(fileTree);
            }
            resultRef.set(result);
        };

        List<File> directories = new ArrayList<>();
        Runnable addDirectoriesToResult = () -> {
            if (directories.isEmpty()) {
                return;
            }

            val fileTree = fileCollectionFactory.apply(new ArrayList<>(directories)).getAsFileTree();
            directories.clear();
            addToResult.accept(fileTree);
        };

        for (val file : files) {
            if (file.isDirectory()) {
                directories.add(file);
            } else if (file.isFile()) {
                addDirectoriesToResult.run();
                addToResult.accept(zipTreeFactory.apply(file));
            }
        }

        addDirectoriesToResult.run();

        val result = resultRef.get();
        if (result == null) {
            return fileCollectionFactory.apply(emptyList()).getAsFileTree();
        }

        return result;
    }

    public static FileTree newClasspathFileTree(Project project, Iterable<File> files) {
        return newClasspathFileTree(
            project::files,
            project::zipTree,
            files
        );
    }

}
