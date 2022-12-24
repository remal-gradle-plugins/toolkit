package name.remal.gradle_plugins.toolkit.issues;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.issues.IssueSeverity.ERROR;
import static name.remal.gradle_plugins.toolkit.issues.IssueSeverity.INFO;
import static name.remal.gradle_plugins.toolkit.issues.IssueSeverity.WARNING;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
abstract class CheckstyleXmlUtils {

    private static final Map<IssueSeverity, String> SEVERITIES = ImmutableMap.<IssueSeverity, String>builder()
        .put(ERROR, "error")
        .put(WARNING, "warning")
        .put(INFO, "info")
        .build();

    @Nullable
    @Contract("null->null")
    public static String getCheckstyleSeverityFor(@Nullable IssueSeverity severity) {
        if (severity == null) {
            return null;
        }

        return SEVERITIES.get(severity);
    }

    @Nullable
    @Contract("null->null")
    public static IssueSeverity getIssueSeverityFor(@Nullable String severity) {
        if (severity == null) {
            return null;
        }

        for (val entry : SEVERITIES.entrySet()) {
            if (severity.equalsIgnoreCase(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

}
