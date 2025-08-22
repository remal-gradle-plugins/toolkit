package name.remal.gradle_plugins.toolkit.testkit.functional;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.gradle.util.GradleVersion;
import org.jspecify.annotations.Nullable;

@Value
@Builder
public class SuppressedMessage {

    boolean startsWith;

    @NonNull
    String message;

    @Nullable
    String stackTracePrefix;

    @Nullable
    GradleVersion minGradleVersion;

    @Nullable
    GradleVersion maxGradleVersion;

}
