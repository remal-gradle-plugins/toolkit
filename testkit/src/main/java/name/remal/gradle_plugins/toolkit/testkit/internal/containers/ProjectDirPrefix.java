package name.remal.gradle_plugins.toolkit.testkit.internal.containers;

import static java.nio.file.Files.createTempDirectory;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.sort;
import static name.remal.gradle_plugins.toolkit.PathUtils.normalizePath;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import lombok.SneakyThrows;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;

@Internal
public class ProjectDirPrefix {

    public static ProjectDirPrefix getProjectDirPrefix(ExtensionStore extensionStore, ExtensionContext context) {
        ProjectDirPrefix prefix = extensionStore.getCurrentStoreValue(context, ProjectDirPrefix.class);
        if (prefix != null) {
            return prefix;
        }

        var parentPrefix = extensionStore.getParentStoreValue(context, ProjectDirPrefix.class);
        if (parentPrefix != null) {
            prefix = parentPrefix.newChildPrefix();
        } else {
            prefix = new ProjectDirPrefix();
        }

        return extensionStore.setCurrentStoreValue(context, prefix);
    }


    private final Deque<String> dirPrefixes = new ArrayDeque<>();

    @Nullable
    private final ProjectDirPrefix parent;

    @VisibleForTesting
    ProjectDirPrefix() {
        this.parent = null;
    }

    private ProjectDirPrefix(ProjectDirPrefix parent) {
        this.parent = parent;
    }

    public ProjectDirPrefix newChildPrefix() {
        return new ProjectDirPrefix(this);
    }

    @Contract("_ -> this")
    @CanIgnoreReturnValue
    public ProjectDirPrefix push(@Nullable String dirPrefix) {
        if (dirPrefix != null && !dirPrefix.isEmpty()) {
            dirPrefixes.addLast(dirPrefix);
        }
        return this;
    }

    @Nullable
    public String pop(String expectedLastDirPrefix) {
        if (dirPrefixes.isEmpty()) {
            return null;
        }

        var lastItem = dirPrefixes.peekLast();
        if (expectedLastDirPrefix.equals(lastItem)) {
            dirPrefixes.removeLast();
        }
        return lastItem;
    }

    private void fillStringBuilder(StringBuilder sb) {
        if (parent != null) {
            parent.fillStringBuilder(sb);
        }

        dirPrefixes.forEach(dirPrefix -> {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(dirPrefix);
        });
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        fillStringBuilder(sb);
        return sb.toString();
    }


    private static final char[] FORBIDDEN_DIR_PREFIX_CHARS = "\\/:<>\"'|?*${}()&[]^".toCharArray();

    static {
        sort(FORBIDDEN_DIR_PREFIX_CHARS);
    }

    @VisibleForTesting
    String getTempDirPrefix() {
        var unescapedPrefix = toString();
        var prefix = new StringBuilder(unescapedPrefix.length() + 1);
        for (int index = 0; index < unescapedPrefix.length(); index++) {
            var ch = unescapedPrefix.charAt(index);
            if (binarySearch(FORBIDDEN_DIR_PREFIX_CHARS, ch) >= 0) {
                prefix.append('-');
            } else if (ch < 32 || ch > 126) {
                prefix.append('-');
            } else {
                prefix.append(ch);
            }
        }

        prefix.append('-');
        return prefix.toString();
    }

    @SneakyThrows
    @SuppressWarnings("java:S5443")
    public Path createTempDir() {
        Path path = createTempDirectory(getTempDirPrefix());
        path = normalizePath(path);
        return path;
    }

}
