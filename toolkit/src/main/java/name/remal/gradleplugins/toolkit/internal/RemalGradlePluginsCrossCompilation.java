package name.remal.gradleplugins.toolkit.internal;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @deprecated DO NOT USE IT DIRECTLY
 */
@Target(TYPE)
@Retention(CLASS)
@Deprecated
public @interface RemalGradlePluginsCrossCompilation {

    String dependency();

    String version();

    String versionOperator();

}
