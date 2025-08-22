package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import java.time.Duration;
import org.jspecify.annotations.Nullable;

public interface WithDefaultTaskTimeout {

    void setDefaultTaskTimeout(@Nullable Duration defaultTaskTimeout);

}
