package name.remal.gradleplugins.toolkit.git;

import static java.lang.String.format;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newInputStream;
import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.ContinuousIntegrationUtils.isRunningOnCi;
import static name.remal.gradleplugins.toolkit.PathUtils.normalizePath;
import static name.remal.gradleplugins.toolkit.git.GitBooleanAttribute.newGitBooleanAttributeBuilder;
import static name.remal.gradleplugins.toolkit.git.GitStringAttribute.newGitStringAttributeBuilder;
import static org.eclipse.jgit.attributes.Attribute.State.CUSTOM;
import static org.eclipse.jgit.attributes.Attribute.State.SET;
import static org.eclipse.jgit.attributes.Attribute.State.UNSET;
import static org.eclipse.jgit.lib.Constants.DOT_GIT;
import static org.eclipse.jgit.lib.Constants.DOT_GIT_ATTRIBUTES;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradleplugins.toolkit.ObjectUtils;
import name.remal.gradleplugins.toolkit.PathUtils;
import org.eclipse.jgit.attributes.AttributesNode;
import org.eclipse.jgit.attributes.AttributesRule;
import org.jetbrains.annotations.Unmodifiable;

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

        if (isRunningOnCi()) {
            val ciProjectPath = Stream.of(
                    "CI_PROJECT_DIR",
                    "GITHUB_WORKSPACE"
                )
                .map(System::getenv)
                .filter(ObjectUtils::isNotEmpty)
                .map(Paths::get)
                .map(PathUtils::normalizePath)
                .filter(path::startsWith)
                .findFirst()
                .orElse(null);
            return ciProjectPath;
        }

        return null;
    }

    @Unmodifiable
    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public static List<GitAttribute> getGitAttributesFor(Path repositoryRoot, String relativePath) {
        repositoryRoot = normalizePath(repositoryRoot);
        val filePath = normalizePath(repositoryRoot.resolve(relativePath));
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
            val gitAttributesFile = currentDir.resolve(DOT_GIT_ATTRIBUTES);
            val rules = parseGitAttributesRules(gitAttributesFile);
            for (val rule : rules) {
                if (!rule.isMatch(relativePath, false)) {
                    continue;
                }

                if (result == null) {
                    result = new LinkedHashMap<>();
                }

                for (val attr : rule.getAttributes()) {
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
                            .value(attr.getValue())
                            .build()
                        );
                    }
                }
            }
            if (result != null) {
                return ImmutableList.copyOf(result.values());
            }

            currentDir = currentDir.getParent();
        }

        return emptyList();
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

        val attributesNode = new AttributesNode();
        try (val inputStream = newInputStream(gitAttributesFile)) {
            attributesNode.parse(inputStream);
        }
        return ImmutableList.copyOf(attributesNode.getRules());
    }


    public static boolean isGitRepositoryRoot(Path path) {
        val dotGit = path.resolve(DOT_GIT);
        return isDirectory(dotGit);
    }

}
