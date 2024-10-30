package name.remal.gradle_plugins.toolkit.annotations;

import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

/**
 * This code relies on Gradle's internal API. Use it with caution, as it can become broken in the future Gradle
 * versions.
 *
 * <p>Use alternative public API approaches whenever possible.</p>
 */
@Retention(CLASS)
@Inherited
@Documented
public @interface ReliesOnInternalGradleApi {
}
