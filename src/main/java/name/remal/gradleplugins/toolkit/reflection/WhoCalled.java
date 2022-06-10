package name.remal.gradleplugins.toolkit.reflection;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class WhoCalled {

    public static Class<?> getCallingClass(int depth) {
        return WhoCalledSecurityManager.INSTANCE.getCallingClass(depth);
    }

    public static boolean isCalledBy(Class<?> type) {
        return WhoCalledSecurityManager.INSTANCE.isCalledBy(type);
    }

}
