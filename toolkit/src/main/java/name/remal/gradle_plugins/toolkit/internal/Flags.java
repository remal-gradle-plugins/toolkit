package name.remal.gradle_plugins.toolkit.internal;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getenv;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class Flags {

    public static final String IS_IN_FUNCTION_TEST_ENV_VAR = "NAME_REMAL_GRADLE_PLUGINS_TOOLKIT_TESTKIT_FUNCTIONAL";

    public static boolean isInFunctionTest() {
        return parseBoolean(getenv(IS_IN_FUNCTION_TEST_ENV_VAR));
    }

}
