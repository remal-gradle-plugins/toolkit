package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.InTestFlags.isInTest;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class CiUtils {

    @Nullable
    private static final CiSystem CI_SYSTEM = StreamSupport.stream(
            ServiceLoader.load(CiSystem.class, CiSystem.class.getClassLoader()).spliterator(),
            false
        )
        .filter(CiSystem::isDetected)
        .findFirst()
        .orElse(null);

    public static Optional<CiSystem> getCiSystem() {
        if (isInTest()) {
            return Optional.empty();
        }

        return Optional.ofNullable(CI_SYSTEM);
    }

    @Contract(pure = true)
    public static boolean isRunningOnCi() {
        return getCiSystem()
            .map(CiSystem::isDetected)
            .orElse(false);
    }

    @Contract(pure = true)
    public static boolean isNotRunningOnCi() {
        return !isRunningOnCi();
    }

}
