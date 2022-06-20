package name.remal.gradleplugins.toolkit.reflection;

import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradleplugins.toolkit.SneakyThrowUtils.sneakyThrow;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.isClassPresent;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class WhoCalledUtils {

    private static final WhoCalled WHO_CALLED;

    static {
        try {
            if (isClassPresent("java.lang.StackWalker", WhoCalledUtils.class.getClassLoader())) {
                WHO_CALLED = (WhoCalled) Class.forName(
                        WhoCalled.class.getName() + "StackWalker",
                        true,
                        WhoCalledUtils.class.getClassLoader()
                    )
                    .getConstructor()
                    .newInstance();

            } else {
                WHO_CALLED = new WhoCalledSecurityManager();
            }

        } catch (Exception e) {
            throw sneakyThrow(e);
        }
    }

    public static Class<?> getCallingClass(int depth) {
        return WHO_CALLED.getCallingClass(depth);
    }

    public static boolean isCalledBy(Class<?> type) {
        return WHO_CALLED.isCalledBy(type);
    }

}
