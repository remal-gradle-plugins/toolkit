package name.remal.gradle_plugins.toolkit;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.ActionUtils.doNothingAction;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.getStaticMethod;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.tryLoadClass;

import groovy.lang.Closure;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import name.remal.gradle_plugins.toolkit.annotations.DynamicCompatibilityCandidate;
import org.gradle.api.Action;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class ClosureUtils {

    @DynamicCompatibilityCandidate
    private static final Class<?> CONFIGURE_UTIL_CLASS = Stream.of(
            "org.gradle.util.internal.ConfigureUtil",
            "org.gradle.util.ConfigureUtil"
        )
        .map(className -> tryLoadClass(className, ClosureUtils.class.getClassLoader()))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow();


    @Contract("_,_->param1")
    @SuppressWarnings("rawtypes")
    public static <T> T configureWith(T object, @Nullable Closure closure) {
        if (closure != null) {
            var configureMethod = getStaticMethod(CONFIGURE_UTIL_CLASS, "configure", Closure.class, Object.class);
            configureMethod.invoke(closure, object);
        }
        return object;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> Action<T> configureUsing(@Nullable Closure closure) {
        if (closure != null) {
            var configureMethod = getStaticMethod(CONFIGURE_UTIL_CLASS, Action.class, "configureUsing", Closure.class);
            return configureMethod.invoke(closure);
        }
        return doNothingAction();
    }

}
