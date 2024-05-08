package name.remal.gradle_plugins.toolkit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;

@Tag("xslt")
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
@Inherited
public @interface TagXslt {
}
