package name.remal.gradleplugins.toolkit.testkit.functional;

import javax.annotation.Nullable;
import lombok.Value;

@Value
public class SuppressedMessage {

    String message;

    @Nullable
    String stackTracePackagePrefix;

}
