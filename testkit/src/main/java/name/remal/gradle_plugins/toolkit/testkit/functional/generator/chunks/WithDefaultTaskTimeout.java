package name.remal.gradle_plugins.toolkit.testkit.functional.generator.chunks;

import java.time.Duration;
import javax.annotation.Nullable;

public interface WithDefaultTaskTimeout {

    void setDefaultTaskTimeout(@Nullable Duration defaultTaskTimeout);

}
