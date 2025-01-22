package name.remal.gradle_plugins.toolkit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

@Retention(RUNTIME)
@Inherited
@Documented
public @interface MaxJavaVersion {

    int value();

}
