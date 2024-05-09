package name.remal.gradle_plugins.toolkit;

import static java.lang.Integer.parseInt;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.gradle.api.JavaVersion;
import org.gradle.util.GradleVersion;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleCompatibilityUtils {

    public static GradleCompatibilityMode getGradleJavaCompatibility(int javaMajorVersion) {
        return GradleCompatibilityJava.get(GradleVersion.current(), javaMajorVersion);
    }

    public static GradleCompatibilityMode getGradleJavaCompatibility(JavaVersion javaVersion) {
        return GradleCompatibilityJava.get(GradleVersion.current(), parseInt(javaVersion.getMajorVersion()));
    }

    public static GradleCompatibilityMode getGradleJavaCompatibility() {
        return getGradleJavaCompatibility(JavaVersion.current());
    }

}
