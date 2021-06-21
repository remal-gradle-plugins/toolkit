package name.remal.gradleplugins.toolkit.reflection;

public interface WhoCalled {

    static Class<?> getCallingClass(int depth) {
        return WhoCalledSecurityManager.INSTANCE.getCallingClass(depth);
    }

    static boolean isCalledBy(Class<?> type) {
        return WhoCalledSecurityManager.INSTANCE.isCalledBy(type);
    }

}
