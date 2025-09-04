package name.remal.gradle_plugins.toolkit.annotations;

import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

@Inherited
@Retention(CLASS)
@Documented
public @interface ConfigurationPhaseOnly {
}
