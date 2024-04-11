package name.remal.gradle_plugins.toolkit.internal;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getenv;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class Flags {

    public static final String IS_IN_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST";
    public static final String IS_IN_FUNCTIONAL_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TEST_FUNCTIONAL";

    public static boolean isInTest() {
        return parseBoolean(getenv(IS_IN_TEST_ENV_VAR))
            || isInFunctionalTest();
    }

    public static boolean isInFunctionalTest() {
        return parseBoolean(getenv(IS_IN_FUNCTIONAL_TEST_ENV_VAR));
    }

}
