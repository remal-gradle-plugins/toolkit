package name.remal.gradle_plugins.toolkit;

import static java.util.Comparator.nullsFirst;
import static java.util.stream.Collectors.joining;
import static lombok.Builder.Default;

import com.google.common.base.Splitter;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@With
class CrossCompileServiceDependencyVersion
    implements Comparable<CrossCompileServiceDependencyVersion> {

    @Default
    String dependency = "";

    @Nullable
    Version version;

    @Default
    boolean earlierIncluded = false;

    @Default
    boolean selfIncluded = false;

    @Default
    boolean laterIncluded = false;

    public boolean isOnlySelfIncluded() {
        return !earlierIncluded && selfIncluded && !laterIncluded;
    }

    public boolean isNothingIncluded() {
        return !earlierIncluded && !selfIncluded && !laterIncluded;
    }


    @SuppressWarnings("UnstableApiUsage")
    public CrossCompileServiceDependencyVersion withMaxVersionNumbersCount(int maxVersionNumbersCount) {
        if (version == null) {
            return this;
        }

        if (!version.hasSuffix() && version.getNumbersCount() <= maxVersionNumbersCount) {
            return this;
        }

        var newVersionString = Splitter.on('.').splitToStream(version.withoutSuffix().toString())
            .limit(maxVersionNumbersCount)
            .collect(joining("."));
        var newVersion = Version.parse(newVersionString);
        return withVersion(newVersion);
    }


    @Override
    public int compareTo(CrossCompileServiceDependencyVersion other) {
        int result = nullsFirst(String::compareTo).compare(this.dependency, other.dependency);
        if (result == 0) {
            result = nullsFirst(Version::compareTo).compare(this.version, other.version);
        }
        if (result == 0) {
            result = Boolean.compare(this.laterIncluded, other.laterIncluded);
        }
        if (result == 0) {
            result = Boolean.compare(this.selfIncluded, other.selfIncluded);
        }
        if (result == 0) {
            result = -1 * Boolean.compare(this.earlierIncluded, other.earlierIncluded);
        }
        return result;
    }

    @SuppressWarnings("java:S3776")
    public boolean intersectsWith(CrossCompileServiceDependencyVersion other) {
        if (!Objects.equals(this.dependency, other.dependency)
            || this.version == null
            || other.version == null
        ) {
            return false;
        }

        if (this.isNothingIncluded() || other.isNothingIncluded()) {
            return false;
        }

        var comparisonResult = this.version.compareTo(other.version);
        if (comparisonResult == 0) {
            if (this.selfIncluded && other.selfIncluded) {
                return true;
            }
        }

        if (comparisonResult < 0) {
            if (this.laterIncluded && other.earlierIncluded) {
                return true;
            }
        }

        if (comparisonResult > 0) {
            return this.earlierIncluded && other.laterIncluded;
        }

        return false;
    }

}
