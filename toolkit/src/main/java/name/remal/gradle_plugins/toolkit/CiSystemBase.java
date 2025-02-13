package name.remal.gradle_plugins.toolkit;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getenv;
import static name.remal.gradle_plugins.toolkit.FileUtils.normalizeFile;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isEmpty;
import static name.remal.gradle_plugins.toolkit.ObjectUtils.isNotEmpty;

import java.io.File;
import java.util.Optional;
import javax.annotation.Nullable;

public abstract class CiSystemBase implements CiSystem {

    @Override
    public final File getBuildDir() {
        return CiSystem.super.getBuildDir();
    }


    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this.getClass().isInstance(obj);
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }


    // region utils

    protected static boolean getBooleanEnv(String name) {
        return parseBoolean(getenv(name));
    }

    protected static boolean isNotEmptyEnv(String name) {
        return isNotEmpty(getenv(name));
    }

    protected static String getRequiredEnv(String name) {
        var env = getenv(name);
        if (isEmpty(env)) {
            throw new IllegalStateException("Environment is not set or empty: " + name);
        }
        return env;
    }

    protected static File getRequiredFileEnv(String name) {
        return normalizeFile(new File(getRequiredEnv(name)));
    }

    @Nullable
    protected static File getOptionalFileEnv(String name) {
        return Optional.ofNullable(getenv(name))
            .filter(ObjectUtils::isNotEmpty)
            .map(File::new)
            .map(FileUtils::normalizeFile)
            .orElse(null);
    }

    // endregion

}
