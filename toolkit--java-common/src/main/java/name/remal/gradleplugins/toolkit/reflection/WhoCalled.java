package name.remal.gradleplugins.toolkit.reflection;

interface WhoCalled {

    Class<?> getCallingClass(int depth);

    boolean isCalledBy(Class<?> type);

}
