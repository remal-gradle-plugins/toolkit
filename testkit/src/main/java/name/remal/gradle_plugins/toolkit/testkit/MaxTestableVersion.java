package name.remal.gradle_plugins.toolkit.testkit;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import name.remal.gradle_plugins.toolkit.testkit.MaxTestableVersion.MaxTestableVersions;
import name.remal.gradle_plugins.toolkit.testkit.internal.MaxTestableVersionExtension;
import org.intellij.lang.annotations.Pattern;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MaxTestableVersionExtension.class)
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
@Repeatable(MaxTestableVersions.class)
@Documented
public @interface MaxTestableVersion {

    String module();

    @Pattern("\\d+(\\.\\d+)+([.+_-].+)?")
    String version();


    @Target({TYPE, METHOD, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Inherited
    @Documented
    @interface MaxTestableVersions {
        MaxTestableVersion[] value();
    }

}
