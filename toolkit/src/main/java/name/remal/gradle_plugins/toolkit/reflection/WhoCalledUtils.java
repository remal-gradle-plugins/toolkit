package name.remal.gradle_plugins.toolkit.reflection;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyProxy;

import java.util.List;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Unmodifiable;

@NoArgsConstructor(access = PRIVATE)
public abstract class WhoCalledUtils {

    private static final WhoCalled METHODS = asLazyProxy(
        WhoCalled.class,
        () -> loadCrossCompileService(WhoCalled.class)
    );

    private static final int LAZY_METHODS_DEPTH_OFFSET = 1;


    @Unmodifiable
    public static List<Class<?>> getCallingClasses(int depth) {
        return METHODS.getCallingClasses(depth + LAZY_METHODS_DEPTH_OFFSET);
    }

    public static Class<?> getCallingClass(int depth) {
        return METHODS.getCallingClass(depth + LAZY_METHODS_DEPTH_OFFSET);
    }

    public static boolean isCalledBy(Class<?> type) {
        return METHODS.isCalledBy(1 + LAZY_METHODS_DEPTH_OFFSET, type);
    }

}
