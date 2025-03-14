package name.remal.gradle_plugins.toolkit.annotations;

import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

/**
 * This code relies on external dependency.
 */
@Retention(CLASS)
@Documented
public @interface ReliesOnExternalDependency {
}
