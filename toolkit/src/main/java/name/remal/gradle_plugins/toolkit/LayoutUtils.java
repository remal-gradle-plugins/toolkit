package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.ProjectUtils.getTopLevelDirOf;
import static name.remal.gradle_plugins.toolkit.git.GitUtils.findGitRepositoryRootFor;
import static org.ec4j.core.EditorConfigConstants.EDITORCONFIG;
import static org.eclipse.jgit.lib.Constants.DOT_GIT_ATTRIBUTES;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import org.gradle.api.Project;

@NoArgsConstructor(access = PRIVATE)
public abstract class LayoutUtils {

    @SuppressWarnings("java:S2259")
    public static Path getRootPathOf(Path topLevelDir) {
        var repositoryRoot = findGitRepositoryRootFor(topLevelDir);
        if (repositoryRoot != null) {
            return repositoryRoot;
        }

        return normalizePath(topLevelDir);
    }

    public static Path getRootPathOf(Project project) {
        var topLevelDir = getTopLevelDirOf(project);
        return getRootPathOf(topLevelDir);
    }

    public static File getRootDirOf(Path topLevelDir) {
        return getRootPathOf(topLevelDir).toFile();
    }

    public static File getRootDirOf(File topLevelDir) {
        return getRootDirOf(topLevelDir.toPath());
    }

    public static File getRootDirOf(Project project) {
        return getRootPathOf(project).toFile();
    }


    private static final List<String> CODE_FORMATTING_FILE_RELATIVE_PATHS = List.of(
        EDITORCONFIG,
        DOT_GIT_ATTRIBUTES
    );

    public static List<Path> getCodeFormattingPathsFor(Project project) {
        var projectPath = normalizePath(project.getProjectDir().toPath());
        var rootPath = getRootPathOf(project);

        List<Path> paths = new ArrayList<>();
        Path currentPath = projectPath;
        do {
            CODE_FORMATTING_FILE_RELATIVE_PATHS.stream()
                .map(currentPath::resolve)
                .forEach(paths::add);
            currentPath = currentPath.getParent();
        } while (currentPath != null && currentPath.startsWith(rootPath));

        return paths;
    }

}
