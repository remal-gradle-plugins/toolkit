package name.remal.gradleplugins.toolkit.reflection;

final class WhoCalledSecurityManager extends SecurityManager {

    public static final WhoCalledSecurityManager INSTANCE = new WhoCalledSecurityManager();

    private static final int OFFSET = 1;

    public Class<?> getCallingClass(int depth) {
        return getClassContext()[OFFSET + depth];
    }

    public boolean isCalledBy(Class<?> type) {
        Class<?>[] classes = getClassContext();
        for (int i = OFFSET + 1; i < classes.length; i++) {
            if (classes[i] == type) {
                return true;
            }
        }

        return false;
    }

}
