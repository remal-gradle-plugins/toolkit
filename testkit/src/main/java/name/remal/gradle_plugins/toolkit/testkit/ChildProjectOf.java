package name.remal.gradle_plugins.toolkit.testkit;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * This annotation is supposed to be used only on parameters.
 *
 * <p>{@link ElementType#FIELD} target is added to
 * simplify Lombok's @{@link RequiredArgsConstructor} and @{@link AllArgsConstructor} annotations usage if
 * <code>lombok.copyableAnnotations += {@link ChildProjectOf}</code> is set in <code>lombokj.config</code> file.</p>
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
public @interface ChildProjectOf {

    /**
     * Parameter name with the injected parent Gradle project
     */
    String value();

}
