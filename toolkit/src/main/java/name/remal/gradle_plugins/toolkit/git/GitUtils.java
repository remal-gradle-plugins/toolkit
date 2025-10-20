package name.remal.gradle_plugins.toolkit.git;

import static java.lang.String.format;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newInputStream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradle_plugins.toolkit.git.GitBooleanAttribute.newGitBooleanAttributeBuilder;
import static name.remal.gradle_plugins.toolkit.git.GitStringAttribute.newGitStringAttributeBuilder;
import static org.eclipse.jgit.attributes.Attribute.State.CUSTOM;
import static org.eclipse.jgit.attributes.Attribute.State.SET;
import static org.eclipse.jgit.attributes.Attribute.State.UNSET;
import static org.eclipse.jgit.lib.Constants.DOT_GIT;
import static org.eclipse.jgit.lib.Constants.DOT_GIT_ATTRIBUTES;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.jgit.attributes.AttributesNode;
import org.eclipse.jgit.attributes.AttributesRule;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class GitUtils {

    @Nullable
    public static Path findGitRepositoryRootFor(Path path) {
        path = normalizePath(path);

        {
            Path currentPath = path;
            while (currentPath != null) {
                if (isGitRepositoryRoot(currentPath)) {
                    return currentPath;
                }
                currentPath = currentPath.getParent();
            }
        }

        return null;
    }

    @Nullable
    public static File findGitRepositoryRootFor(File file) {
        var path = findGitRepositoryRootFor(file.toPath());
        return path != null ? path.toFile() : null;
    }


    @Unmodifiable
    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public static List<GitAttribute> getGitAttributesFor(Path repositoryRoot, String relativePath) {
        repositoryRoot = normalizePath(repositoryRoot);
        var filePath = normalizePath(repositoryRoot.resolve(relativePath));
        if (!filePath.startsWith(repositoryRoot)) {
            throw new IllegalArgumentException(format(
                "Path is outside of the repository root (%s): %s",
                repositoryRoot,
                relativePath
            ));
        }

        Path currentDir = filePath.getParent();
        while (currentDir != null && currentDir.startsWith(repositoryRoot)) {
            Map<String, GitAttribute> result = null;
            var gitAttributesFile = currentDir.resolve(DOT_GIT_ATTRIBUTES);
            var rules = parseGitAttributesRules(gitAttributesFile);
            for (var rule : rules) {
                if (!rule.isMatch(relativePath, false)) {
                    continue;
                }

                if (result == null) {
                    result = new LinkedHashMap<>();
                }

                for (var attr : rule.getAttributes()) {
                    if (attr.getState() == SET) {
                        result.put(attr.getKey(), newGitBooleanAttributeBuilder()
                            .name(attr.getKey())
                            .set(true)
                            .build()
                        );
                    } else if (attr.getState() == UNSET) {
                        result.put(attr.getKey(), newGitBooleanAttributeBuilder()
                            .name(attr.getKey())
                            .set(false)
                            .build()
                        );
                    } else if (attr.getState() == CUSTOM) {
                        result.put(attr.getKey(), newGitStringAttributeBuilder()
                            .name(attr.getKey())
                            .value(requireNonNullElse(attr.getValue(), ""))
                            .build()
                        );
                    }
                }
            }
            if (result != null) {
                return List.copyOf(result.values());
            }

            currentDir = currentDir.getParent();
        }

        return emptyList();
    }

    public static List<GitAttribute> getGitAttributesFor(File repositoryRoot, String relativePath) {
        return getGitAttributesFor(repositoryRoot.toPath(), relativePath);
    }

    private static List<AttributesRule> parseGitAttributesRules(Path gitAttributesFile) {
        return GIT_ATTRIBUTES_RULES_CACHE.computeIfAbsent(gitAttributesFile, GitUtils::parseGitAttributesRulesImpl);
    }

    private static final ConcurrentMap<Path, List<AttributesRule>> GIT_ATTRIBUTES_RULES_CACHE =
        new ConcurrentHashMap<>();

    @SneakyThrows
    private static List<AttributesRule> parseGitAttributesRulesImpl(Path gitAttributesFile) {
        if (!isRegularFile(gitAttributesFile)) {
            return emptyList();
        }

        var attributesNode = new AttributesNode();
        try (var inputStream = newInputStream(gitAttributesFile)) {
            attributesNode.parse(inputStream);
        }
        return List.copyOf(attributesNode.getRules());
    }


    public static boolean isGitRepositoryRoot(Path path) {
        var dotGit = path.resolve(DOT_GIT);
        var notRootRepoMarker = path.resolve(".git-not-root");
        return isDirectory(dotGit) && !isRegularFile(notRootRepoMarker);
    }

    public static boolean isGitRepositoryRoot(File file) {
        return isGitRepositoryRoot(file.toPath());
    }
}
