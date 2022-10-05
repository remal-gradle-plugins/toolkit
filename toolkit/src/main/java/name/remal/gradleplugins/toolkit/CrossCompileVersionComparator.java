package name.remal.gradleplugins.toolkit;

import javax.annotation.Nullable;
import lombok.val;

@FunctionalInterface
public interface CrossCompileVersionComparator {

    /**
     * @return {@code null} if the dependency is not supported
     */
    @Nullable
    Integer compareVersion(String dependency, String versionString) throws Throwable;


    default CrossCompileVersionComparator then(CrossCompileVersionComparator other) {
        return (dependency, versionString) -> {
            val thisResult = this.compareVersion(dependency, versionString);
            if (thisResult != null) {
                return thisResult;
            } else {
                return other.compareVersion(dependency, versionString);
            }
        };
    }

}
