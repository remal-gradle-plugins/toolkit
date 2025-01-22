package name.remal.gradle_plugins.toolkit;

import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.build_time_constants.api.BuildTimeConstants.getClassName;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.tryLoadClass;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.VerificationException;

@NoArgsConstructor(access = PRIVATE)
public abstract class VerificationExceptionUtils {

    @SuppressWarnings("unchecked")
    private static final List<Class<GradleException>> VERIFICATION_EXCEPTION_CLASSES = Stream.of(
            getClassName(VerificationException.class),
            getClassName(GradleException.class)
        )
        .map(className -> tryLoadClass(className, VerificationExceptionUtils.class.getClassLoader()))
        .filter(Objects::nonNull)
        .map(clazz -> (Class<GradleException>) clazz)
        .collect(toUnmodifiableList());

    @ForBackwardCompatibilityWithGradle("7.3")
    @SneakyThrows
    public static GradleException newVerificationException(String message) {
        var ctor = VERIFICATION_EXCEPTION_CLASSES.stream()
            .map(clazz -> {
                try {
                    return clazz.getConstructor(String.class);
                } catch (NoSuchMethodException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow();
        return ctor.newInstance(message);
    }

    @ForBackwardCompatibilityWithGradle("8.1")
    @SneakyThrows
    public static GradleException newVerificationException(String message, Throwable cause) {
        var ctor = VERIFICATION_EXCEPTION_CLASSES.stream()
            .map(clazz -> {
                try {
                    return clazz.getConstructor(String.class, Throwable.class);
                } catch (NoSuchMethodException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow();
        return ctor.newInstance(message, cause);
    }

}
