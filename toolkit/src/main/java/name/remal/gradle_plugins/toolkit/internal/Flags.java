package name.remal.gradle_plugins.toolkit.internal;

import static java.lang.Boolean.parseBoolean;
import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class Flags {

    public static final String IS_IN_FUNCTION_TEST_FLAG = "name.remal.gradle_plugins.toolkit.testkit.functional";

    public static boolean isInFunctionTest() {
        return parseBoolean(System.getProperty(IS_IN_FUNCTION_TEST_FLAG));
    }

}
