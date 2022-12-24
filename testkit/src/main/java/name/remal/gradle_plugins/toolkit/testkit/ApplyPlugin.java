package name.remal.gradle_plugins.toolkit.testkit;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import name.remal.gradle_plugins.toolkit.testkit.ApplyPlugin.ApplyPlugins;
import org.gradle.api.Plugin;

/**
 * <p>Applies Gradle plugins for projects injected by {@link GradleProjectExtension}.</p>
 * <p>&nbsp;</p>
 * <p><small>This annotation is supposed to be used only on parameters. {@link ElementType#FIELD} target is added to
 * simplify Lombok's @{@link RequiredArgsConstructor} and @{@link AllArgsConstructor} annotations usage if
 * <code>lombok.copyableAnnotations += {@link ApplyPlugin}</code></small>
 * is set in <code>lombokj.config</code> file.</p>
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
@Repeatable(ApplyPlugins.class)
public @interface ApplyPlugin {

    String value() default "";

    Class<? extends Plugin<?>> type() default NotSetPluginType.class;

    interface NotSetPluginType extends Plugin<Object> { }


    @Target({PARAMETER, FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface ApplyPlugins {
        ApplyPlugin[] value();
    }

}
