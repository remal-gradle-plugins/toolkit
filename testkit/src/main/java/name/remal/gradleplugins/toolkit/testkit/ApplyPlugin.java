package name.remal.gradleplugins.toolkit.testkit;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.gradle.api.Plugin;

/**
 * Applies Gradle plugins for projects injected by {@link GradleProjectExtension}.
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
public @interface ApplyPlugin {

    String[] id() default {};

    Class<? extends Plugin<?>>[] type() default {};

}
