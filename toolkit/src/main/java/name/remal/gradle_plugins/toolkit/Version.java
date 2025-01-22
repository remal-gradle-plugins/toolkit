package name.remal.gradle_plugins.toolkit;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import javax.annotation.Nullable;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.DefaultVersionComparator;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionParser;
import org.jetbrains.annotations.Contract;

@ReliesOnInternalGradleApi
public final class Version implements Comparable<Version> {

    private static final VersionParser PARSER = new VersionParser();

    @Contract(pure = true)
    public static Version parse(String version) {
        var versionImpl = PARSER.transform(version);
        return new Version(requireNonNull(versionImpl));
    }


    private final org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.Version versionImpl;

    private Version(org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.Version versionImpl) {
        this.versionImpl = versionImpl;
    }


    @Nullable
    public Long getNumberOrNull(int numberIndex) {
        if (numberIndex < 0) {
            throw new IllegalArgumentException("numberIndex: must >= 0");
        }

        var parts = versionImpl.getNumericParts();
        return numberIndex < parts.length ? parts[numberIndex] : null;
    }

    public long getNumber(int numberIndex) {
        var number = getNumberOrNull(numberIndex);
        if (number == null) {
            throw new IllegalArgumentException(format(
                "Version '%s' doesn't have numeric part with index %d",
                versionImpl.getSource(),
                numberIndex
            ));
        }
        return number;
    }

    public int getNumbersCount() {
        return versionImpl.getNumericParts().length;
    }


    public boolean hasSuffix() {
        return versionImpl.isQualified();
    }

    public Version withoutSuffix() {
        return versionImpl.isQualified() ? new Version(versionImpl.getBaseVersion()) : this;
    }


    private static final Comparator<org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.Version> COMPARATOR
        = new DefaultVersionComparator().asVersionComparator();

    @Override
    public int compareTo(Version other) {
        return COMPARATOR.compare(this.versionImpl, other.versionImpl);
    }

    public boolean isLessThan(Version other) {
        return compareTo(other) < 0;
    }

    public boolean isLessThan(String other) {
        var otherVersion = parse(other);
        return isLessThan(otherVersion);
    }

    public boolean isLessOrEqualTo(Version other) {
        return compareTo(other) <= 0;
    }

    public boolean isLessOrEqualTo(String other) {
        var otherVersion = parse(other);
        return isLessOrEqualTo(otherVersion);
    }

    public boolean isGreaterOrEqualTo(Version other) {
        return compareTo(other) >= 0;
    }

    public boolean isGreaterOrEqualTo(String other) {
        var otherVersion = parse(other);
        return isGreaterOrEqualTo(otherVersion);
    }

    public boolean isGreaterThan(Version other) {
        return compareTo(other) > 0;
    }

    public boolean isGreaterThan(String other) {
        var otherVersion = parse(other);
        return isGreaterThan(otherVersion);
    }


    @Override
    public String toString() {
        return versionImpl.getSource();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof Version)) {
            return false;
        }

        Version version = (Version) other;
        return versionImpl.getSource().equals(version.versionImpl.getSource());
    }

    @Override
    public int hashCode() {
        return versionImpl.getSource().hashCode();
    }

}
