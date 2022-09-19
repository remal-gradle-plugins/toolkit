package name.remal.gradleplugins.toolkit;

import static java.util.Comparator.nullsFirst;
import static java.util.stream.Collectors.joining;
import static lombok.Builder.Default;

import com.google.common.base.Splitter;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.val;

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


    @SuppressWarnings("UnstableApiUsage")
    public CrossCompileServiceDependencyVersion withMaxVersionNumbersCount(int maxVersionNumbersCount) {
        if (version == null) {
            return this;
        }

        if (!version.hasSuffix() && version.getNumbersCount() <= maxVersionNumbersCount) {
            return this;
        }

        val newVersionString = Splitter.on('.').splitToStream(version.withoutSuffix().toString())
            .limit(maxVersionNumbersCount)
            .collect(joining("."));
        val newVersion = Version.parse(newVersionString);
        return withVersion(newVersion);
    }


    @Override
    public int compareTo(CrossCompileServiceDependencyVersion other) {
        int result = nullsFirst(String::compareTo).compare(this.dependency, other.dependency);
        if (result == 0) {
            result = nullsFirst(Version::compareTo).compare(this.version, other.version);
        }
        return result;
    }

    public boolean intersectsWith(CrossCompileServiceDependencyVersion other) {
        if (!Objects.equals(this.dependency, other.dependency)
            || this.version == null
            || other.version == null
        ) {
            return false;
        }

        val comparisonResult = this.version.compareTo(other.version);
        if (comparisonResult < 0) {
            return this.laterIncluded || other.earlierIncluded;

        } else if (comparisonResult > 0) {
            return this.earlierIncluded || other.laterIncluded;

        } else {
            return this.selfIncluded == other.selfIncluded;
        }
    }

}
