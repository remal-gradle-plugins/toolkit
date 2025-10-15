package name.remal.gradle_plugins.toolkit;

import org.jspecify.annotations.Nullable;

public interface CiSystemDetector extends Comparable<CiSystemDetector> {

    @Nullable
    CiSystem detect();


    default int getOrder() {
        return 0;
    }

    @Override
    default int compareTo(CiSystemDetector other) {
        var result = Integer.compare(getOrder(), other.getOrder());
        if (result == 0) {
            result = getClass().getName().compareTo(other.getClass().getName());
        }
        return result;
    }

}
