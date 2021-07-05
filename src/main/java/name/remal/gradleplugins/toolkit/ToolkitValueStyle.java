package name.remal.gradleplugins.toolkit;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.BuilderVisibility;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Target({TYPE, PACKAGE})
@Retention(SOURCE)
@Value.Style(
    defaults = @Value.Immutable(
        copy = false
    ),
    visibility = ImplementationVisibility.SAME,
    builderVisibility = BuilderVisibility.PUBLIC,
    jdkOnly = true,
    get = {"is*", "get*"},
    optionalAcceptNullable = true,
    privateNoargConstructor = true,
    typeBuilder = "*Builder",
    typeInnerBuilder = "BaseBuilder",
    allowedClasspathAnnotations = {
        javax.annotation.processing.Generated.class,
        org.immutables.value.Generated.class,
        Nullable.class,
        Immutable.class,
        ThreadSafe.class,
        NotThreadSafe.class,
    },
    depluralize = true
)
@SuppressWarnings("Since15")
public @interface ToolkitValueStyle {
}
