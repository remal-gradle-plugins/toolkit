package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ConfigurationCacheSafeSystem.EnvironmentVariableNotSetOrEmptyException;
import static name.remal.gradle_plugins.toolkit.InTestFlags.isInTest;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class CiUtils {

    @Nullable
    private static final CiSystem CI_SYSTEM = StreamSupport.stream(
            ServiceLoader.load(CiSystemDetector.class, CiSystemDetector.class.getClassLoader()).spliterator(),
            false
        )
        .sorted()
        .map(detector -> {
            try {
                return detector.detect();
            } catch (EnvironmentVariableNotSetOrEmptyException ignored) {
                return null; // can't detect this CI system
            }
        })
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);

    private static Optional<CiSystem> getDetectedCiSystem() {
        return Optional.ofNullable(CI_SYSTEM);
    }


    @Contract(pure = true)
    public static Optional<CiSystem> getCiSystem() {
        if (isInTest()) {
            return Optional.empty();
        }

        return getDetectedCiSystem();
    }

    @Contract(pure = true)
    public static boolean isRunningOnCi() {
        return getCiSystem().isPresent();
    }

    @Contract(pure = true)
    public static boolean isNotRunningOnCi() {
        return !isRunningOnCi();
    }

    /**
     * Do not use it in plugin production code.
     *
     * <p>Returns {@code true} if the current process runs on a CI system, even under a test.
     *
     * <p>Unlike {@link CiUtils#isRunningOnCi()}, this method reports the real CI environment
     * regardless of whether the code runs inside a test. It is intended for test-harness and
     * build-infrastructure code only.
     */
    @Contract(pure = true)
    public static boolean isRunningOnCiIncludingTests() {
        return getDetectedCiSystem().isPresent();
    }

}
