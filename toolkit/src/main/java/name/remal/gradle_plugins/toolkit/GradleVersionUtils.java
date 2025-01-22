package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleVersionUtils {

    private static final GradleVersion CURRENT = GradleVersion.current().getBaseVersion();


    @Contract(pure = true)
    public static boolean isCurrentGradleVersionLessThan(GradleVersion version) {
        return CURRENT.compareTo(version.getBaseVersion()) < 0;
    }

    @Contract(pure = true)
    public static boolean isCurrentGradleVersionLessThan(String versionString) {
        var version = GradleVersion.version(versionString);
        return isCurrentGradleVersionLessThan(version);
    }


    @Contract(pure = true)
    public static boolean isCurrentGradleVersionLessThanOrEqualTo(GradleVersion version) {
        return CURRENT.compareTo(version.getBaseVersion()) <= 0;
    }

    @Contract(pure = true)
    public static boolean isCurrentGradleVersionLessThanOrEqualTo(String versionString) {
        var version = GradleVersion.version(versionString);
        return isCurrentGradleVersionLessThanOrEqualTo(version);
    }


    @Contract(pure = true)
    public static boolean isCurrentGradleVersionEqualTo(GradleVersion version) {
        return CURRENT.compareTo(version.getBaseVersion()) == 0;
    }

    @Contract(pure = true)
    public static boolean isCurrentGradleVersionEqualTo(String versionString) {
        var version = GradleVersion.version(versionString);
        return isCurrentGradleVersionEqualTo(version);
    }


    @Contract(pure = true)
    public static boolean isCurrentGradleVersionGreaterThanOrEqualTo(GradleVersion version) {
        return CURRENT.compareTo(version.getBaseVersion()) >= 0;
    }

    @Contract(pure = true)
    public static boolean isCurrentGradleVersionGreaterThanOrEqualTo(String versionString) {
        var version = GradleVersion.version(versionString);
        return isCurrentGradleVersionGreaterThanOrEqualTo(version);
    }


    @Contract(pure = true)
    public static boolean isCurrentGradleVersionGreaterThan(GradleVersion version) {
        return CURRENT.compareTo(version.getBaseVersion()) > 0;
    }

    @Contract(pure = true)
    public static boolean isCurrentGradleVersionGreaterThan(String versionString) {
        var version = GradleVersion.version(versionString);
        return isCurrentGradleVersionGreaterThan(version);
    }

}
