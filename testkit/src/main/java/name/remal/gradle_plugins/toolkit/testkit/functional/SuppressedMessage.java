package name.remal.gradle_plugins.toolkit.testkit.functional;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import org.gradle.util.GradleVersion;

@Value
@Builder
public class SuppressedMessage {

    boolean startsWith;

    String message;

    @Nullable
    String stackTracePrefix;

    @Nullable
    GradleVersion minGradleVersion;

    @Nullable
    GradleVersion maxGradleVersion;

}
