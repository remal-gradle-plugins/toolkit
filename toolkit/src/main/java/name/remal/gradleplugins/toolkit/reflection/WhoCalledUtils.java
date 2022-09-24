package name.remal.gradleplugins.toolkit.reflection;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.CrossCompileServices.loadCrossCompileService;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class WhoCalledUtils {

    private static final WhoCalled WHO_CALLED = loadCrossCompileService(WhoCalled.class);

    public static Class<?> getCallingClass(int depth) {
        return WHO_CALLED.getCallingClass(depth);
    }

    public static boolean isCalledBy(Class<?> type) {
        return WHO_CALLED.isCalledBy(type);
    }

}
