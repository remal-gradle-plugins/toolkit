package name.remal.gradleplugins.toolkit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import org.intellij.lang.annotations.Pattern;

@Retention(RUNTIME)
@Inherited
@Documented
public @interface MinGradleVersion {

    @Pattern("\\d+(\\.\\d+)+")
    String value();

}
