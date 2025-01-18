package name.remal.gradle_plugins.toolkit;

import static java.lang.Integer.parseInt;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import org.gradle.api.JavaVersion;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class GradleCompatibilityUtils {

    @Contract(pure = true)
    public static GradleCompatibilityMode getGradleJavaCompatibility(int javaMajorVersion) {
        return GradleCompatibilityJava.get(GradleVersion.current(), javaMajorVersion);
    }

    @Contract(pure = true)
    public static GradleCompatibilityMode getGradleJavaCompatibility(JavaVersion javaVersion) {
        return GradleCompatibilityJava.get(GradleVersion.current(), parseInt(javaVersion.getMajorVersion()));
    }

    @Contract(pure = true)
    public static GradleCompatibilityMode getGradleJavaCompatibility() {
        return getGradleJavaCompatibility(JavaVersion.current());
    }

}
