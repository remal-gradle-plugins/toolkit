package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.val;
import org.gradle.util.GradleVersion;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleVersionUtils {

    private static final GradleVersion CURRENT = GradleVersion.current();


    public static boolean isCurrentGradleVersionLessThan(GradleVersion version) {
        return CURRENT.compareTo(version) < 0;
    }

    public static boolean isCurrentGradleVersionLessThan(String versionString) {
        val version = GradleVersion.version(versionString);
        return isCurrentGradleVersionLessThan(version);
    }


    public static boolean isCurrentGradleVersionLessThanOrEqualTo(GradleVersion version) {
        return CURRENT.compareTo(version) <= 0;
    }

    public static boolean isCurrentGradleVersionLessThanOrEqualTo(String versionString) {
        val version = GradleVersion.version(versionString);
        return isCurrentGradleVersionLessThanOrEqualTo(version);
    }


    public static boolean isCurrentGradleVersionEqualTo(GradleVersion version) {
        return CURRENT.compareTo(version) == 0;
    }

    public static boolean isCurrentGradleVersionEqualTo(String versionString) {
        val version = GradleVersion.version(versionString);
        return isCurrentGradleVersionEqualTo(version);
    }


    public static boolean isCurrentGradleVersionGreaterThanOrEqualTo(GradleVersion version) {
        return CURRENT.compareTo(version) >= 0;
    }

    public static boolean isCurrentGradleVersionGreaterThanOrEqualTo(String versionString) {
        val version = GradleVersion.version(versionString);
        return isCurrentGradleVersionGreaterThanOrEqualTo(version);
    }


    public static boolean isCurrentGradleVersionGreaterThan(GradleVersion version) {
        return CURRENT.compareTo(version) > 0;
    }

    public static boolean isCurrentGradleVersionGreaterThan(String versionString) {
        val version = GradleVersion.version(versionString);
        return isCurrentGradleVersionGreaterThan(version);
    }

}
