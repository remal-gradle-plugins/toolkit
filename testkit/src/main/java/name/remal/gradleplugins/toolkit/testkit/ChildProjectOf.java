package name.remal.gradleplugins.toolkit.testkit;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
public @interface ChildProjectOf {

    /**
     * Parameter name with parent Gradle project
     */
    String value();

}
