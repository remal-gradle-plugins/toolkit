package name.remal.gradleplugins.toolkit.testkit.internal.containers;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.extension.ExtensionContext;

@Internal
public class ProjectDirPrefix {

    public static ProjectDirPrefix getProjectDirPrefix(ExtensionStore extensionStore, ExtensionContext context) {
        ProjectDirPrefix prefix = extensionStore.getCurrentStoreValue(context, ProjectDirPrefix.class);
        if (prefix != null) {
            return prefix;
        }

        val parentPrefix = extensionStore.getParentStoreValue(context, ProjectDirPrefix.class);
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

    private ProjectDirPrefix() {
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
            dirPrefix = normalizeDirPrefix(dirPrefix);
            dirPrefixes.addLast(dirPrefix);
        }
        return this;
    }

    @Nullable
    public String pop(String expectedLastDirPrefix) {
        if (dirPrefixes.isEmpty()) {
            return null;
        }

        expectedLastDirPrefix = normalizeDirPrefix(expectedLastDirPrefix);
        val lastItem = dirPrefixes.peekLast();
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
        val sb = new StringBuilder();
        fillStringBuilder(sb);
        return sb.toString();
    }


    private static final Pattern FORBIDDEN_DIR_PREFIX_CHARS = Pattern.compile("[\\\\/:<>\"'|?*]");

    private static String normalizeDirPrefix(String dirPrefix) {
        return FORBIDDEN_DIR_PREFIX_CHARS.matcher(dirPrefix).replaceAll("-");
    }

}
