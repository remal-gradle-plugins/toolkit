package name.remal.gradle_plugins.toolkit.testkit.functional;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import org.gradle.util.GradleVersion;

@Value
@Builder
public class SuppressedMessage {

    @Default
    boolean startsWith = false;

    String message;

    @Nullable
    String stackTracePrefix;

    @Nullable
    GradleVersion minGradleVersion;

    @Nullable
    GradleVersion maxGradleVersion;

}
