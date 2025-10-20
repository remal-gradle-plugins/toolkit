package name.remal.gradle_plugins.toolkit;

import static java.lang.Boolean.parseBoolean;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;

import java.io.File;
import java.lang.reflect.Method;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = PRIVATE)
public abstract class ConfigurationCacheSafeSystem {

    private static final Method GETENV;

    static {
        try {
            GETENV = System.class.getMethod("getenv", String.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Nullable
    @Contract(pure = true)
    @SneakyThrows
    public static String getConfigurationCacheSafeOptionalEnv(String name) {
        return (String) GETENV.invoke(null, name);
    }

    @Contract(pure = true)
    public static String getConfigurationCacheSafeRequiredEnv(String name) {
        var env = getConfigurationCacheSafeOptionalEnv(name);
        if (env == null || env.isEmpty()) {
            throw new EnvironmentVariableNotSetOrEmptyException(name);
        }
        return env;
    }

    @Contract(pure = true)
    public static boolean isConfigurationCacheSafeNotEmptyEnv(String name) {
        return isNotEmpty(getConfigurationCacheSafeOptionalEnv(name));
    }

    @Contract(pure = true)
    public static boolean getConfigurationCacheSafeBooleanEnv(String name) {
        return parseBoolean(getConfigurationCacheSafeOptionalEnv(name));
    }

    @Contract(pure = true)
    public static File getConfigurationCacheSafeRequiredFileEnv(String name) {
        var filePath = getConfigurationCacheSafeRequiredEnv(name);
        return normalizeFile(new File(filePath));
    }

    @Nullable
    @Contract(pure = true)
    @SuppressWarnings("java:S2259")
    public static File getConfigurationCacheSafeOptionalFileEnv(String name) {
        var filePath = getConfigurationCacheSafeOptionalEnv(name);
        if (filePath == null) {
            return null;
        }
        return normalizeFile(new File(filePath));
    }


    public static class EnvironmentVariableNotSetOrEmptyException extends IllegalStateException {
        EnvironmentVariableNotSetOrEmptyException(String name) {
            super("Environment is not set or empty: " + name);
        }
    }

}
