package name.remal.gradle_plugins.toolkit.internal;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @deprecated <b>DO NOT USE IT DIRECTLY</b>
 */
@Target(TYPE)
@Retention(CLASS)
@Deprecated(since = "always")
public @interface RemalGradlePluginsCrossCompilation {

    String dependency();

    String version();

    String versionOperator();

}
