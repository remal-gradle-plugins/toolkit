package name.remal.gradleplugins.testkit;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import name.remal.gradleplugins.testkit.MinSupportedVersion.MinSupportedVersions;
import name.remal.gradleplugins.testkit.internal.MinSupportedVersionExtension;
import org.intellij.lang.annotations.Pattern;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinSupportedVersionExtension.class)
@Target({TYPE, METHOD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Repeatable(MinSupportedVersions.class)
@Inherited
@Documented
public @interface MinSupportedVersion {

    String module();

    @Pattern("\\d+(\\.\\d+)+([.+_-].+)?")
    String version();


    @Target({TYPE, METHOD, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Inherited
    @Documented
    @interface MinSupportedVersions {
        MinSupportedVersion[] value();
    }

}
