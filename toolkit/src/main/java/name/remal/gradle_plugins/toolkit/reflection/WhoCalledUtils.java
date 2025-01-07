package name.remal.gradle_plugins.toolkit.reflection;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;

import java.util.List;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Unmodifiable;

@NoArgsConstructor(access = PRIVATE)
public abstract class WhoCalledUtils {

    private static final WhoCalled WHO_CALLED = loadCrossCompileService(WhoCalled.class);

    @Unmodifiable
    public static List<Class<?>> getCallingClasses(int depth) {
        return WHO_CALLED.getCallingClasses(depth);
    }

    public static Class<?> getCallingClass(int depth) {
        return WHO_CALLED.getCallingClass(depth);
    }

    public static boolean isCalledBy(Class<?> type) {
        return WHO_CALLED.isCalledBy(type);
    }

}
