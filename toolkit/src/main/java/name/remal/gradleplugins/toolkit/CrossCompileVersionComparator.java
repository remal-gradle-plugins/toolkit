package name.remal.gradleplugins.toolkit;

import javax.annotation.Nullable;
import lombok.val;

@FunctionalInterface
public interface CrossCompileVersionComparator {

    enum CrossCompileVersionComparisonResult {
        DEPENDENCY_GREATER_THAN_CURRENT,
        DEPENDENCY_EQUALS_TO_CURRENT,
        DEPENDENCY_LESS_THAN_CURRENT,
        ;

        public static <
            T extends Comparable<T>
            > CrossCompileVersionComparisonResult compareDependencyVersionToCurrentVersionObjects(
            T dependencyVersionObject,
            T currentVersionObject
        ) {
            val result = dependencyVersionObject.compareTo(currentVersionObject);
            if (result < 0) {
                return DEPENDENCY_LESS_THAN_CURRENT;
            } else if (result == 0) {
                return DEPENDENCY_EQUALS_TO_CURRENT;
            } else {
                return DEPENDENCY_GREATER_THAN_CURRENT;
            }
        }
    }


    /**
     * @return {@code null} if the dependency is not supported
     */
    @Nullable
    CrossCompileVersionComparisonResult compareDependencyVersionToCurrentVersion(
        String dependency,
        String dependencyVersionString
    ) throws Throwable;


    default CrossCompileVersionComparator then(CrossCompileVersionComparator other) {
        return (dependency, dependencyVersionString) -> {
            val thisResult = this.compareDependencyVersionToCurrentVersion(dependency, dependencyVersionString);
            if (thisResult != null) {
                return thisResult;
            } else {
                return other.compareDependencyVersionToCurrentVersion(dependency, dependencyVersionString);
            }
        };
    }

}
