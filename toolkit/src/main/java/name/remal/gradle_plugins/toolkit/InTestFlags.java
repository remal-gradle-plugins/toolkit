package name.remal.gradle_plugins.toolkit;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getenv;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class InTestFlags {

    public static final String IS_IN_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST";
    public static final String IS_IN_UNIT_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST_UNIT";
    public static final String IS_IN_INTEGRATION_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST_INTEGRATION";
    public static final String IS_IN_FUNCTIONAL_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST_FUNCTIONAL";

    public static boolean isInTest() {
        return parseBoolean(getenv(IS_IN_TEST_ENV_VAR));
    }

    public static boolean isInUnitTest() {
        return parseBoolean(getenv(IS_IN_UNIT_TEST_ENV_VAR));
    }

    public static boolean isInIntegrationTest() {
        return parseBoolean(getenv(IS_IN_INTEGRATION_TEST_ENV_VAR));
    }

    public static boolean isInFunctionalTest() {
        return parseBoolean(getenv(IS_IN_FUNCTIONAL_TEST_ENV_VAR));
    }

}
