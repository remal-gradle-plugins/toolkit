package name.remal.gradle_plugins.toolkit;

import static java.lang.Math.min;

public enum CrossCompileVersionComparisonResult {

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
        var result = dependencyVersionObject.compareTo(currentVersionObject);
        if (result < 0) {
            return DEPENDENCY_LESS_THAN_CURRENT;
        } else if (result == 0) {
            return DEPENDENCY_EQUALS_TO_CURRENT;
        } else {
            return DEPENDENCY_GREATER_THAN_CURRENT;
        }
    }

    public static CrossCompileVersionComparisonResult compareDependencyVersionToCurrentVersionObjects(
        long[] dependencyVersionNumbers,
        long[] currentVersionNumbers
    ) {
        var commonLength = min(dependencyVersionNumbers.length, currentVersionNumbers.length);
        for (int i = 0; i < commonLength; ++i) {
            var dependencyVersionNumber = dependencyVersionNumbers[i];
            var currentVersionNumber = currentVersionNumbers[i];
            if (dependencyVersionNumber < currentVersionNumber) {
                return DEPENDENCY_LESS_THAN_CURRENT;
            } else if (dependencyVersionNumber > currentVersionNumber) {
                return DEPENDENCY_GREATER_THAN_CURRENT;
            }
        }

        if (dependencyVersionNumbers.length < currentVersionNumbers.length) {
            return DEPENDENCY_LESS_THAN_CURRENT;
        } else if (dependencyVersionNumbers.length > currentVersionNumbers.length) {
            return DEPENDENCY_GREATER_THAN_CURRENT;
        }

        return DEPENDENCY_EQUALS_TO_CURRENT;
    }

}
