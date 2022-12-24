package name.remal.gradle_plugins.toolkit;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.reflection.MembersFinder.getStaticMethod;

import groovy.lang.Closure;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class ClosureUtils {

    @Contract("_,_->param1")
    @SuppressWarnings("rawtypes")
    public static <T> T configureWith(T object, @Nullable Closure closure) {
        if (closure != null) {
            val configureUtilClass = getConfigureUtilClass();
            val configureMethod = getStaticMethod(configureUtilClass, "configure", Closure.class, Object.class);
            configureMethod.invoke(closure, object);
        }
        return object;
    }


    @ReliesOnInternalGradleApi
    private static final List<String> CONFIGURE_UTIL_CLASS_NAMES = unmodifiableList(asList(
        "org.gradle.util.internal.ConfigureUtil",
        "org.gradle.util.ConfigureUtil"
    ));

    private static Class<?> getConfigureUtilClass() {
        for (val className : CONFIGURE_UTIL_CLASS_NAMES) {
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
