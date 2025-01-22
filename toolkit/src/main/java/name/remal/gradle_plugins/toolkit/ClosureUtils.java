package name.remal.gradle_plugins.toolkit;

import static java.lang.String.join;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ActionUtils.doNothingAction;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.getStaticMethod;

import groovy.lang.Closure;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.gradle.api.Action;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class ClosureUtils {

    @Contract("_,_->param1")
    @SuppressWarnings("rawtypes")
    public static <T> T configureWith(T object, @Nullable Closure closure) {
        if (closure != null) {
            var configureUtilClass = getConfigureUtilClass();
            var configureMethod = getStaticMethod(configureUtilClass, "configure", Closure.class, Object.class);
            configureMethod.invoke(closure, object);
        }
        return object;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> Action<T> configureUsing(@Nullable Closure closure) {
        if (closure != null) {
            var configureUtilClass = getConfigureUtilClass();
            var configureMethod = getStaticMethod(configureUtilClass, Action.class, "configureUsing", Closure.class);
            return configureMethod.invoke(closure);
        }
        return doNothingAction();
    }


    @ReliesOnInternalGradleApi
    private static final List<String> CONFIGURE_UTIL_CLASS_NAMES = List.of(
        "org.gradle.util.internal.ConfigureUtil",
        "org.gradle.util.ConfigureUtil"
    );

    private static Class<?> getConfigureUtilClass() {
        for (var className : CONFIGURE_UTIL_CLASS_NAMES) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException expected) {
                // do nothing
            }
        }

        throw new IllegalStateException(
            "ConfigureUtil class can't be found. Candidates: " + join(", ", CONFIGURE_UTIL_CLASS_NAMES)
        );
    }

}
