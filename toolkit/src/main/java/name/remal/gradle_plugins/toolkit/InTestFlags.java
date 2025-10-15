package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ConfigurationCacheSafeSystem.isConfigurationCacheSafeNotEmptyEnv;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class InTestFlags {

    public static final String IS_IN_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST";
    public static final String IS_IN_UNIT_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST_UNIT";
    public static final String IS_IN_INTEGRATION_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST_INTEGRATION";
    public static final String IS_IN_COMPONENT_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST_COMPONENT";
    public static final String IS_IN_FUNCTIONAL_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST_FUNCTIONAL";

    @Contract(pure = true)
    public static boolean isInTest() {
        return isConfigurationCacheSafeNotEmptyEnv(IS_IN_TEST_ENV_VAR);
    }

    @Contract(pure = true)
    public static boolean isInUnitTest() {
        return isConfigurationCacheSafeNotEmptyEnv(IS_IN_UNIT_TEST_ENV_VAR);
    }

    @Contract(pure = true)
    public static boolean isInIntegrationTest() {
        return isConfigurationCacheSafeNotEmptyEnv(IS_IN_INTEGRATION_TEST_ENV_VAR);
    }

    @Contract(pure = true)
    public static boolean isInComponentTest() {
        return isConfigurationCacheSafeNotEmptyEnv(IS_IN_COMPONENT_TEST_ENV_VAR);
    }

    @Contract(pure = true)
    public static boolean isInFunctionalTest() {
        return isConfigurationCacheSafeNotEmptyEnv(IS_IN_FUNCTIONAL_TEST_ENV_VAR);
    }

}
