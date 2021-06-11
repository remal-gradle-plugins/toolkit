package name.remal.gradleplugins.testkit.internal.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.extension.ExtensionContext;

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


    private static final Pattern FORBIDDEN_DIR_PREFIX_CHARS = Pattern.compile("[\\\\/:<>\"'|?*]");

    private final Collection<String> dirPrefixes = new ArrayList<>();

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
    public ProjectDirPrefix push(@Nullable String dirPrefix) {
        if (dirPrefix != null && !dirPrefix.isEmpty()) {
            dirPrefix = FORBIDDEN_DIR_PREFIX_CHARS.matcher(dirPrefix).replaceAll("-");
            dirPrefixes.add(dirPrefix);
        }
        return this;
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

}
