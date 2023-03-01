package name.remal.gradle_plugins.toolkit.testkit.functional;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

@Value
@Builder
public class SuppressedMessage {

    @Default
    boolean startsWith = false;

    String message;

    @Nullable
    @Default
    String stackTracePrefix = null;

}
