package name.remal.gradle_plugins.toolkit;

import java.io.File;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public interface CiSystem {

    default String getName() {
        return this.getClass().getSimpleName();
    }

    boolean isDetected();

    @OverridingMethodsMustInvokeSuper
    default File getBuildDir() {
        var buildDir = getBuildDirIfSupported();
        if (buildDir == null) {
            throw new UnsupportedOperationException();
        }
        return buildDir;
    }

    @Nullable
    default File getBuildDirIfSupported() {
        return null;
    }

}
