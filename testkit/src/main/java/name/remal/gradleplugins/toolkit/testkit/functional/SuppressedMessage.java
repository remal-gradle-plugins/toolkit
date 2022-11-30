package name.remal.gradleplugins.toolkit.testkit.functional;

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
    String stackTracePackagePrefix = null;

}
