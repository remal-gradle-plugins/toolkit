package name.remal.gradleplugins.toolkit;

import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

/**
 * This code relies on Gradle's internal API. Use it with caution, as it can become broken in the future Gradle
 * versions.
 */
@Retention(CLASS)
@Documented
public @interface ReliesOnInternalGradleApi {
}
