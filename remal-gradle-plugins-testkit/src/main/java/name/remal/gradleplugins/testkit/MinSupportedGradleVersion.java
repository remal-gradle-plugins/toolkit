package name.remal.gradleplugins.testkit;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import name.remal.gradleplugins.testkit.internal.MinSupportedGradleVersionExtension;
import org.intellij.lang.annotations.Pattern;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinSupportedGradleVersionExtension.class)
@Target({TYPE, METHOD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Inherited
@Documented
public @interface MinSupportedGradleVersion {

    @Pattern("\\d+(\\.\\d+)+")
    String value();

}
