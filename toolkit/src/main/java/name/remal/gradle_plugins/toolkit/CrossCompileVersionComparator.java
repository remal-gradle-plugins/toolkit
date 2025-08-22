package name.remal.gradle_plugins.toolkit;

import static java.lang.Math.min;
import static name.remal.gradle_plugins.toolkit.CrossCompileVersionComparisonResult.compareDependencyVersionToCurrentVersionObjects;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface CrossCompileVersionComparator {


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
            var thisResult = this.compareDependencyVersionToCurrentVersion(dependency, dependencyVersionString);
            if (thisResult != null) {
                return thisResult;
            } else {
                return other.compareDependencyVersionToCurrentVersion(dependency, dependencyVersionString);
            }
        };
    }


    static CrossCompileVersionComparator standardVersionCrossCompileVersionComparator(
        String dependency,
        String currentDependencyVersionString
    ) {
        var currentDependencyVersion = Version.parse(currentDependencyVersionString).withoutSuffix();
        return (dependencyToCheck, dependencyVersionToCheckString) -> {
            if (dependency.equals(dependencyToCheck)) {
                var dependencyVersionToCheck = Version.parse(dependencyVersionToCheckString).withoutSuffix();
                if (dependencyVersionToCheck.getNumberOrNull(0) == null) {
                    // not a numeric version, just compare
                    return compareDependencyVersionToCurrentVersionObjects(
                        dependencyVersionToCheck,
                        currentDependencyVersion
                    );
                }

                var versionNumbers = dependencyVersionToCheck.getNumbersCount();
                var dependencyVersionToCheckNumbers = new long[versionNumbers];
                for (int i = 0; i < min(dependencyVersionToCheck.getNumbersCount(), versionNumbers); ++i) {
                    dependencyVersionToCheckNumbers[i] = dependencyVersionToCheck.getNumber(i);
                }
                var currentDependencyVersionNumbers = new long[versionNumbers];
                for (int i = 0; i < min(currentDependencyVersion.getNumbersCount(), versionNumbers); ++i) {
                    currentDependencyVersionNumbers[i] = currentDependencyVersion.getNumber(i);
                }
                return compareDependencyVersionToCurrentVersionObjects(
                    dependencyVersionToCheckNumbers,
                    currentDependencyVersionNumbers
                );
            }

            return null;
        };
    }

}
